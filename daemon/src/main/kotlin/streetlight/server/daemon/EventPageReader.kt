package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toDataOr
import streetlight.agent.LMProblem
import streetlight.agent.parseHtmlDocument
import streetlight.model.data.EventPageSchema
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.Origin
import streetlight.model.data.ParseMode
import streetlight.server.model.ContentParse
import streetlight.server.routes.SchemaParserText

class EventPageReader(
    private val locationConfig: LocationConfigContent,
    private val origin: Origin,
    private val initialUrl: Url,
    private val daemon: ParseDaemon
) {
    val koog get() = daemon.koog
    val dao get() = daemon.dao
    val log get() = daemon.log

    suspend fun read(): RawEvent? {
        val gate = daemon.getRobotGate(origin)
        val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode
        val fetch = gate.fetchWhenOpen(initialUrl, fetchMode).toDataOr(daemon::logProblem) { return null }
        val doc = parseHtmlDocument(fetch.text, fetch.pageUrl).toDataOr(daemon::logProblem) { return null }
        val pageUrl = dao.link.registerFetch(origin.originId, doc, fetch)

        val report = doc.readStructuredData()
        if (report.jsonLdBlocks > 0 || report.microdataEventCount > 0) {
            log.info { "structured data at ${fetch.pageUrl}: blocks=${report.jsonLdBlocks} " +
                    "malformed=${report.jsonLdMalformed} events=${report.eventCount} " +
                    "microdataEvents=${report.microdataEventCount} types=${report.types}" }
        }

        val pageSchema = origin.getPageSchema(fetch.pageUrl, doc).toDataOr(daemon::logProblem) { return null }
        return parsePageEvent(pageSchema, doc, locationConfig.config.parseMode, pageUrl).also {
            println("Cost: ${it.cost}")
        }
    }

    private suspend fun Origin.getPageSchema(url: Url, doc: Document): Outcome<EventPageSchema> {
        val parsers = dao.parser.read(originId)
        parsers.sortedByDescending { it.lastSuccessAt }.mapNotNull { schema ->
            val content = schema.schema as? EventPageSchema ?: return@mapNotNull null
            val pageEvent = parsePageEvent(content, doc, ParseMode.Full)
            val isSuccess = !pageEvent.title.isNullOrBlank() && !pageEvent.descriptionHtml.isNullOrBlank()

            val length = pageEvent.descriptionHtml?.length
            // println(length) td: a better way to score quality of selector

            if (!isSuccess) {
                dao.parser.updateResult(schema.parserId, false)
                return@mapNotNull null
            }

            length to schema
        }.sortedByDescending { it.first }.firstOrNull()?.let { (length, schema) ->
            println("chose: $length")
            dao.parser.updateResult(schema.parserId, true)
            return Ok(schema.schema as EventPageSchema)
        }

        if (daemon.lmUsageLimitReached) return LMProblem.UsageLimit

        // td: likewise limit LM call by interval
        val content = koog.readHtml<ContentParse<EventPageSchema>>(
            url = url, doc = doc, instructions = SchemaParserText.EventPageSelectorsInstructions
        ).toDataOr {
            if (it == LMProblem.UsageLimit) {
                daemon.lmUsageLimitReached = true
            }
            return it
        }

        val contentSchema = content.content
        if (!content.isExpectedContent || contentSchema == null) {
            return Problem("Document content was not an event page")
        }
        if (content.isIncompleteContent) {
            log.info { "Found incomplete content: $url" }
            dao.origin.registerIncomplete(originId)
        }

        dao.parser.create(originId, contentSchema, fetchMode)
        return Ok(contentSchema)
    }

    private fun parsePageEvent(
        schema: EventPageSchema,
        doc: Document,
        parseMode: ParseMode,
        pageUrl: Url? = null,
    ): RawEvent {
        val body = doc.body()
        return RawEvent(
            title = body.queryElement(schema.title).takeIf { it.isPlausibleField() }.plainText(),
            url = pageUrl,
            image = body.queryElement(schema.image).absoluteUrl("src"),
            descriptionHtml = body.queryElement(schema.description)
                .takeIf { parseMode == ParseMode.Full && it.isPlausibleProse() }.innerHtml(),
            contact = body.queryElement(schema.contact).takeIf { it.isPlausibleField() }.plainText(),
            cost = body.queryElement(schema.cost).takeIf { it.isPlausibleField() }.plainText(),
            ageMin = body.queryElement(schema.ageMin).takeIf { it.isPlausibleField() }.plainText(),
            date = body.queryElement(schema.date).takeIf { it.isPlausibleField() }.plainText(),
            startTime = body.queryElement(schema.startTime).takeIf { it.isPlausibleField() }.plainText(),
            endTime = body.queryElement(schema.endTime).takeIf { it.isPlausibleField() }.plainText(),
        )
    }
}