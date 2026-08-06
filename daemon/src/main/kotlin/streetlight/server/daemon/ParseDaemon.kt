package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import io.github.oshai.kotlinlogging.KotlinLogging
import kampfire.api.Slug
import kampfire.api.toMarkdown
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toUrl
import klutch.server.provide
import koala.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.agent.KoogParserClient
import streetlight.agent.LMProblem
import streetlight.agent.StreetlightAgent
import streetlight.agent.fetchText
import streetlight.agent.parseHtmlDocument
import streetlight.agent.tryQuery
import streetlight.model.data.EventEdit
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventPageSchema
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.Origin
import streetlight.model.data.OriginSchema
import streetlight.model.data.PostEdit
import streetlight.model.data.PostType
import streetlight.model.data.toOriginId
import streetlight.server.model.ContentParse
import streetlight.server.model.Server
import streetlight.server.plugins.logger
import streetlight.server.routes.SchemaParserText
import streetlight.server.routes.createEvent
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ParseDaemon(private val server: Server) {

    private val dao = server.dao
    private val koog = server.provide<KoogParserClient>()
    private var lmUsageLimitReached = false

    suspend fun start() {
        while (true) {
            val galaxy = dao.galaxy.readGalaxy(Slug("tag"), null) ?: return
            val locations = dao.location.readCheckable(checkInterval - 1.hours)
            locations.forEach { location ->
                checkLocation(location, galaxy)
            }
            logger.info { "completed location check" }
            delay(1.minutes)
        }
    }

    private suspend fun checkLocation(location: Location, galaxy: Galaxy) {
        dao.location.updateCheckedAt(location.locationId)

        val feedUrl = requireNotNull(location.eventsUrl)
        val originId = feedUrl.toOriginId() ?: return

        val origin = dao.origin.readOrCreateOrigin(originId)
        val gate = origin.getRobotGate()

        val feedHtml = gate.fetchWhenOpen(feedUrl, origin.fetchMode).toDataOr(::logProblem) { return }
        val feedDoc = parseHtmlDocument(feedHtml, feedUrl).toDataOr(::logProblem) { return }
        val feedSchema = origin.getFeedSchema(feedUrl, feedDoc).toDataOr(::logProblem) { return }

        val eventSelector = feedSchema.event ?: return
        val body = feedDoc.body()
        val pageElements = body.tryQuery(eventSelector).toDataOr(::logProblem) { return }
        var pageFailCount = 0
        val schemaCache = SchemaCache(origin.schemas.filter { it.fetchMode == origin.fetchMode })

        val edits = pageElements.mapNotNull { element ->
            val feedEvent = RawEvent(
                title = element.queryElement(feedSchema.title).plainText(),
                image = element.queryElement(feedSchema.image).absoluteUrl("src"),
                descriptionHtml = element.queryElement(feedSchema.description)
                    .takeIf { it.isPlausibleProse() }.innerHtml(),
                cost = element.queryElement(feedSchema.cost).plainText(),
                date = element.queryElement(feedSchema.date).plainText(),
                startTime = element.queryElement(feedSchema.time).plainText(),
            )
            val pageUrl = element.queryElement(feedSchema.link).absoluteUrl("href")?.toUrl()
            val pageEvent = pageUrl?.let { url ->
                if (pageFailCount > 0) return@let null
                val pageHtml = gate.fetchWhenOpen(url, origin.fetchMode).toDataOr(::logProblem) { return@let null }
                val pageDoc = parseHtmlDocument(pageHtml, url).toDataOr(::logProblem) { return@let null }

                val pageSchema = origin.getPageSchema(url, pageDoc, schemaCache).toDataOr(::logProblem) {
                    pageFailCount++
                    return@let null
                }
                parsePageEvent(pageSchema, pageDoc)
            }

            println("Cost: ${pageEvent?.cost}")

            val event = RawEvent(
                title = pageEvent?.title ?: feedEvent.title,
                image = pageEvent?.image ?: feedEvent.image,
                descriptionHtml = pageEvent?.descriptionHtml ?: feedEvent.descriptionHtml,
                contact = pageEvent?.contact,
                cost = pageEvent?.cost ?: feedEvent.cost,
                ageMin = pageEvent?.ageMin,
                date = pageEvent?.date ?: feedEvent.date,
                startTime = pageEvent?.startTime ?: feedEvent.startTime,
                endTime = pageEvent?.endTime,
            )

            event.toEventEdit(pageUrl, location.timezoneId, location.locationId).also {
                // println(prettyPrint(it))
            }
        }

        edits.forEach { edit ->
            val startsAt = edit.startsAt ?: return@forEach
            val existingEvent = dao.event.readEventAt(location.locationId, startsAt)
            if (existingEvent != null) return@forEach
            val event = server.createEvent(null, edit).toDataOr { return@forEach }
            dao.post.create(
                PostEdit(
                    postId = null,
                    galaxyId = galaxy.galaxyId,
                    postType = PostType.Event,
                    recordId = event.eventId.value,
                ), null
            )
        }
    }

    fun logProblem(problem: Problem) {
        logger.info { problem.message }
    }

    fun logProblem(message: String) = logProblem(Problem(message))

    private suspend fun Origin.getRobotGate(): RobotGate {
        robotsTxt?.let {
            return it.toRobotGate()
        }
        val txt = fetchText(originId.toRobotsTxtUrl()).toDataOrNull() ?: return RobotGate(null, StreetlightAgent.AgentToken)
        dao.origin.updateRobotsTxt(originId, txt)
        return txt.toRobotGate()
    }

    private suspend fun Origin.getFeedSchema(url: Url, doc: Document): Outcome<EventFeedSchema> {
        val body = doc.body()
        schemas.forEach { schema ->
            val content = schema.selector as? EventFeedSchema ?: return@forEach
            val eventSelector = content.event ?: return@forEach
            val pageElements = body.tryQuery(eventSelector).toDataOr { return@forEach }
            val isSuccess = !pageElements.isEmpty()
            dao.origin.updateSchemaResult(schema.originSchemaId, isSuccess)
            if (isSuccess) return Ok(content)
        }

        if (lmUsageLimitReached) return LMProblem.UsageLimit

        // td: limit LM call by interval
        val content = koog.readHtml<ContentParse<EventFeedSchema>>(
            url = url, doc = doc, instructions = SchemaParserText.EventFeedSelectorsInstructions
        ).toDataOr {
            if (it == LMProblem.UsageLimit) {
                lmUsageLimitReached = true
            }
            return it
        }

        val contentSchema = content.content
        if (!content.isExpectedContent || contentSchema == null) {
            return Problem("Document content was not an event feed")
        }

        dao.origin.create(originId, contentSchema, fetchMode)
        return Ok(contentSchema)
    }

    private suspend fun Origin.getPageSchema(
        url: Url,
        doc: Document,
        schemaCache: SchemaCache
    ): Outcome<EventPageSchema> {

        schemaCache.ordered().mapNotNull { schema ->
            val content = schema.selector as? EventPageSchema ?: return@mapNotNull null
            val pageEvent = parsePageEvent(content, doc)
            val isSuccess = !pageEvent.title.isNullOrBlank() && !pageEvent.descriptionHtml.isNullOrBlank()

            val length = pageEvent.descriptionHtml?.length
            // println(length) td: a better way to score quality of selector

            if (!isSuccess) {
                dao.origin.updateSchemaResult(schema.originSchemaId, false)
                return@mapNotNull null
            }

            length to schema
        }.sortedByDescending { it.first }.firstOrNull()?.let { (length, schema) ->
            println("chose: $length")
            dao.origin.updateSchemaResult(schema.originSchemaId, true)
            return Ok(schema.selector as EventPageSchema)
        }

        if (lmUsageLimitReached) return LMProblem.UsageLimit

        // td: likewise limit LM call by interval
        val content = koog.readHtml<ContentParse<EventPageSchema>>(
            url = url, doc = doc, instructions = SchemaParserText.EventPageSelectorsInstructions
        ).toDataOr {
            if (it == LMProblem.UsageLimit) {
                lmUsageLimitReached = true
            }
            return it
        }

        val contentSchema = content.content
        if (!content.isExpectedContent || contentSchema == null) {
            return Problem("Document content was not an event page")
        }

        val originSchema = dao.origin.create(originId, contentSchema, fetchMode)
        schemaCache.add(originSchema)
        return Ok(contentSchema)
    }

    private fun parsePageEvent(schema: EventPageSchema, doc: Document): RawEvent {
        val body = doc.body()
        return RawEvent(
            title = body.queryElement(schema.title).takeIf { it.isPlausibleField() }.plainText(),
            image = body.queryElement(schema.image).absoluteUrl("src"),
            descriptionHtml = body.queryElement(schema.description).takeIf { it.isPlausibleProse() }.innerHtml(),
            contact = body.queryElement(schema.contact).takeIf { it.isPlausibleField() }.plainText(),
            cost = body.queryElement(schema.cost).takeIf { it.isPlausibleField() }.plainText(),
            ageMin = body.queryElement(schema.ageMin).takeIf { it.isPlausibleField() }.plainText(),
            date = body.queryElement(schema.date).takeIf { it.isPlausibleField() }.plainText(),
            startTime = body.queryElement(schema.startTime).takeIf { it.isPlausibleField() }.plainText(),
            endTime = body.queryElement(schema.endTime).takeIf { it.isPlausibleField() }.plainText(),
        )
    }

    private class SchemaCache(initial: List<OriginSchema>) {
        private val schemas = initial.toMutableList()

        fun ordered(): List<OriginSchema> = schemas.sortedByDescending { it.lastSuccessAt }

        fun add(schema: OriginSchema) = schemas.add(schema)
    }
}

fun CoroutineScope.startParseDaemon(server: Server) {
    launch {
        ParseDaemon(server).start()
        closeBrowser()
    }
}

data class RawEvent(
    val title: String? = null,
    val image: String? = null,
    val descriptionHtml: String? = null,
    val contact: String? = null,
    val cost: String? = null,
    val ageMin: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
)

private val checkInterval = 24.hours

private val logger = KotlinLogging.logger(ParseDaemon::class)

private fun RawEvent.toEventEdit(website: Url?, timeZoneId: String?, locationId: LocationId): EventEdit {

    val dateTimeText = listOfNotNull(date, startTime.takeIf { it != date })
        .joinToString(" ")
        .takeIf { it.isNotBlank() }

    val description = descriptionHtml?.let { htmlToMarkdown(it) }
    val start = dateTimeText?.let { parseLocalDateTime(it, timeZoneId) }
    val end = endTime?.let { parseTimeFromText(it) }

    // val notes = listOfNotNull(
    //     end?.let { "* **Ends:** $it" },
    //     cost?.let { "* **Cost:** $it" },
    //     ageMin?.let { "* **Ages:** $it" },
    // )
    // td: escape markdown
    val timeNote = if (start == null) (startTime ?: date)?.let { "**Time:** $it" } else null

    val body = listOfNotNull(
        timeNote,
        description,
        // notes.joinToString("\n").takeIf { it.isNotBlank() },
    ).joinToString("\n\n").takeIf { it.isNotBlank() }

    return EventEdit(
        locationId = locationId,
        title = title,
        description = body?.toMarkdown(),
        contact = contact, // td: gather phone/email/social media separately
        website = website,
        image = image?.let { Image(it.toUrl()) },
        date = start?.date,
        startTime = start?.time,
        endTime = end,
        timeZoneId = timeZoneId,
        // td: parse ageMin
    )
}

sealed interface FieldResult {
    data object Absent : FieldResult
    data class Invalid(val text: String, val reason: String) : FieldResult
    data class Valid(val value: String, val confidence: Double) : FieldResult
}

//         val chromeFields = setOfNotNull(
//            "descriptionHtml".takeIf { gathered.isConstant { event -> event.descriptionHtml } },
//            "title".takeIf { gathered.isConstant { event -> event.title } },
//            "date".takeIf { gathered.isConstant { event -> event.date } },
//        )
//        if (chromeFields.isNotEmpty()) println("suspected chrome, identical across feed: $chromeFields")
//
//        gathered.forEach { raw ->
//            val judged = raw.copy(
//                descriptionHtml = raw.descriptionHtml.takeIf { "descriptionHtml" !in chromeFields },
//                title = raw.title.takeIf { "title" !in chromeFields },
//                date = raw.date.takeIf { "date" !in chromeFields },
//            )
//            val edit = judged.toEventEdit(null, location.timezoneId, location.locationId)
//            println(prettyPrint(edit))
//        }