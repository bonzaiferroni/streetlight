package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import streetlight.agent.LMProblem
import streetlight.agent.SchemaProblem
import streetlight.agent.isPlausibleField
import streetlight.agent.isPlausibleProse
import streetlight.agent.absoluteUrl
import streetlight.agent.innerHtml
import streetlight.agent.plainText
import streetlight.agent.parseHtmlDocument
import streetlight.agent.queryElement
import streetlight.model.data.EventPageSchema
import streetlight.model.data.FetchMode
import streetlight.model.data.EventFeedSource
import streetlight.model.data.Origin
import streetlight.model.data.SchemaType
import streetlight.server.utils.readImageUrl

class EventPageReader(
    private val source: EventFeedSource,
    private val origin: Origin,
    private val initialUrl: Url,
    private val daemon: ParseDaemon,
    private val tracker: PageTracker,
) {
    val dao get() = daemon.dao
    val log get() = daemon.log

    suspend fun read(): RawEvent? {
        val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode
        return read(fetchMode)
    }

    /** Reads the page fetched in [fetchMode], and again with scripting when its content is incomplete. */
    private suspend fun read(fetchMode: FetchMode): RawEvent? {
        val gate = daemon.getRobotGate(origin)
        val fetch = gate.fetchWhenOpen(initialUrl, fetchMode).toDataOr {
            daemon.logProblem(it)
            tracker.fetchFailed(it)
            return null
        }

        val doc = parseHtmlDocument(fetch.text, fetch.pageUrl).toDataOrNull(daemon::logProblem)
        tracker.fetched(fetchMode, fetch, doc)
        if (doc == null) return null
        val pageUrl = dao.registerFetch(origin.originId, doc, fetch)

        val report = doc.readStructuredData()
        if (report.jsonLdBlocks > 0 || report.microdataEventCount > 0) {
            log.info { "structured data at ${fetch.pageUrl}: blocks=${report.jsonLdBlocks} " +
                    "malformed=${report.jsonLdMalformed} events=${report.eventCount} " +
                    "microdataEvents=${report.microdataEventCount} types=${report.types}" }
        }

        val pageSchema = daemon.mediator.pageSchema(
            url = fetch.pageUrl,
            doc = doc,
            store = OriginSchemaStore(dao, origin),
            timeZoneId = source.timeZoneId,
            allowLm = !daemon.lmUsageLimitReached,
            observer = tracker,
        ).toDataOr {
            daemon.logProblem(it)
            if (it == LMProblem.UsageLimit) daemon.lmUsageLimitReached = true
            tracker.schemaFailed(it)
            if (it == SchemaProblem.Incomplete && fetchMode == FetchMode.Basic) return read(FetchMode.Scripting)
            return null
        }
        tracker.read(SchemaType.EventPage)
        return parsePageEvent(pageSchema, doc, pageUrl).also {
            log.debug { "Parsed ${fetch.pageUrl}: description ${it.descriptionHtml?.length ?: 0} chars, cost ${it.cost}" }
        }
    }

    private fun parsePageEvent(
        schema: EventPageSchema,
        doc: Document,
        pageUrl: Url? = null,
    ): RawEvent {
        val body = doc.body()
        return RawEvent(
            title = body.queryElement(schema.title) { it.isPlausibleField() }.plainText(),
            url = pageUrl,
            image = doc.readImageUrl()?.value ?: body.queryElement(schema.image).absoluteUrl("src"),
            descriptionHtml = body.queryElement(schema.description) { it.isPlausibleProse() }.innerHtml(),
            contact = body.queryElement(schema.contact) { it.isPlausibleField() }.plainText(),
            cost = body.queryElement(schema.cost) { it.isPlausibleField() }.plainText(),
            ageMin = body.queryElement(schema.ageMin) { it.isPlausibleField() }.plainText(),
            date = body.dateText(schema.date, schema.month, schema.day) { it.isPlausibleField() },
            startTime = body.queryElement(schema.startTime) { it.isPlausibleField() }.plainText(),
            endTime = body.queryElement(schema.endTime) { it.isPlausibleField() }.plainText(),
            location = body.queryElement(schema.location) { it.isPlausibleField() }.plainText(),
            address = body.queryElement(schema.address) { it.isPlausibleField() }.plainText(),
        )
    }
}
