package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toUrl
import streetlight.agent.LMProblem
import streetlight.agent.parseHtmlDocument
import streetlight.agent.tryQuery
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.Location
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.Origin
import streetlight.model.data.toOriginId
import streetlight.server.model.ContentParse
import streetlight.server.routes.SchemaParserText

class EventFeedReader(
    private val location: LocationConfigContent,
    private val origin: Origin,
    private val initialUrl: Url,
    private val daemon: ParseDaemon
) {
    val koog get() = daemon.koog
    val dao get() = daemon.dao
    val log get() = daemon.log

    suspend fun read(): List<RawEvent>? {
        val link = dao.link.readLinkByAlias(initialUrl)
        val url = link?.let {
            if (link.fetchedAt >= daemon.startedAt) return null
            link.url
        } ?: initialUrl
        val gate = daemon.getRobotGate(origin)
        val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode
        val fetch = gate.fetchWhenOpen(url, fetchMode).toDataOr(daemon::logProblem) { return null }
        val doc = parseHtmlDocument(fetch.text, fetch.pageUrl).toDataOr(daemon::logProblem) { return null }
        dao.link.registerFetch(origin.originId, doc, fetch)

        val feedSchema = origin.getFeedSchema(url, doc).toDataOr(daemon::logProblem) { return null }
        val eventSelector = feedSchema.event ?: return null
        val body = doc.body()
        val pageElements = body.tryQuery(eventSelector).toDataOr(daemon::logProblem) { return null }

        return pageElements.mapNotNull { element ->
            val feedEvent = RawEvent(
                title = element.queryElement(feedSchema.title).plainText(),
                url = element.queryElement(feedSchema.link).absoluteUrl("href")?.toUrl()?.normalize(),
                image = element.queryElement(feedSchema.image).absoluteUrl("src"),
                descriptionHtml = element.queryElement(feedSchema.description)
                    .takeIf { it.isPlausibleProse() }.innerHtml(),
                cost = element.queryElement(feedSchema.cost).plainText(),
                date = element.queryElement(feedSchema.date).plainText(),
                startTime = element.queryElement(feedSchema.time).plainText(),
            )

            val pageUrl = feedEvent.url ?: return@mapNotNull feedEvent
            val pageLink = dao.link.readLinkByAlias(pageUrl)
            if (pageLink != null) return@mapNotNull null
            val pageOrigin = pageUrl.toOriginId()?.takeIf { it != origin.originId }?.let {
                dao.origin.readOrCreateOrigin(it)
            } ?: origin
            val reader = EventPageReader(location, pageOrigin, pageUrl, daemon)
            val pageEvent = reader.read()

            RawEvent(
                title = pageEvent?.title ?: feedEvent.title,
                url = pageUrl,
                image = pageEvent?.image ?: feedEvent.image,
                descriptionHtml = pageEvent?.descriptionHtml ?: feedEvent.descriptionHtml,
                contact = pageEvent?.contact,
                cost = pageEvent?.cost ?: feedEvent.cost,
                ageMin = pageEvent?.ageMin,
                date = pageEvent?.date ?: feedEvent.date,
                startTime = pageEvent?.startTime ?: feedEvent.startTime,
                endTime = pageEvent?.endTime,
            )
        }
    }

    private suspend fun Origin.getFeedSchema(url: Url, doc: Document): Outcome<EventFeedSchema> {
        val body = doc.body()
        val parsers = dao.parser.read(originId)
        parsers.sortedByDescending { it.lastSuccessAt }.forEach { parser ->
            val schema = parser.schema as? EventFeedSchema ?: return@forEach
            val eventSelector = schema.event ?: return@forEach
            val pageElements = body.tryQuery(eventSelector).toDataOr { return@forEach }
            val isSuccess = !pageElements.isEmpty()
            dao.parser.updateResult(parser.parserId, isSuccess)
            if (isSuccess) return Ok(schema)
        }

        if (daemon.lmUsageLimitReached) return LMProblem.UsageLimit

        // td: limit LM call by interval
        val content = koog.readHtml<ContentParse<EventFeedSchema>>(
            url = url, doc = doc, instructions = SchemaParserText.EventFeedSelectorsInstructions
        ).toDataOr {
            if (it == LMProblem.UsageLimit) {
                daemon.lmUsageLimitReached = true
            }
            return it
        }

        val contentSchema = content.content
        if (!content.isExpectedContent || contentSchema == null) {
            return Problem("Document content was not an event feed")
        }
        if (content.isIncompleteContent) {
            log.info { "Found incomplete content: $url" }
            dao.origin.registerIncomplete(originId)
        }

        dao.parser.create(originId, contentSchema, fetchMode)
        return Ok(contentSchema)
    }
}