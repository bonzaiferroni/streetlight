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

/** The event read from its page at [initialUrl], found in the feed of [source], or null when it is not read. */
suspend fun Crawler.crawlEventPage(
    source: EventFeedSource,
    origin: Origin,
    initialUrl: Url,
    tracker: PageTracker,
): RawEvent? {
    val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode
    return crawlEventPage(source, origin, initialUrl, fetchMode, tracker)
}

/** The event of the page at [initialUrl] fetched in [fetchMode], fetched again with scripting when its content is incomplete. */
private suspend fun Crawler.crawlEventPage(
    source: EventFeedSource,
    origin: Origin,
    initialUrl: Url,
    fetchMode: FetchMode,
    tracker: PageTracker,
): RawEvent? {
    val gate = getRobotGate(origin)
    val fetch = gate.fetchWhenOpen(initialUrl, fetchMode).toDataOr {
        logProblem(it)
        tracker.fetchFailed(it)
        return null
    }

    val doc = parseHtmlDocument(fetch.text, fetch.pageUrl).toDataOrNull(this::logProblem)
    tracker.fetched(fetchMode, fetch, doc)
    if (doc == null) return null
    val pageUrl = dao.registerFetch(origin.originId, doc, fetch)

    val report = doc.readStructuredData()
    if (report.jsonLdBlocks > 0 || report.microdataEventCount > 0) {
        log.info { "structured data at ${fetch.pageUrl}: blocks=${report.jsonLdBlocks} " +
                "malformed=${report.jsonLdMalformed} events=${report.eventCount} " +
                "microdataEvents=${report.microdataEventCount} types=${report.types}" }
    }

    val pageSchema = mediator.pageSchema(
        url = fetch.pageUrl,
        doc = doc,
        store = OriginSchemaStore(dao, origin),
        timeZoneId = source.timeZoneId,
        allowLm = !lmUsageLimitReached,
        observer = tracker,
    ).toDataOr {
        logProblem(it)
        if (it == LMProblem.UsageLimit) lmUsageLimitReached = true
        tracker.schemaFailed(it)
        if (it == SchemaProblem.Incomplete && fetchMode == FetchMode.Basic) return crawlEventPage(source, origin, initialUrl, FetchMode.Scripting, tracker)
        return null
    }
    tracker.read(SchemaType.EventPage)
    return parsePageEvent(pageSchema, doc, pageUrl).also {
        log.debug { "Parsed ${fetch.pageUrl}: description ${it.descriptionHtml?.length ?: 0} chars, cost ${it.cost}" }
    }
}

/** The event read from [doc] by [schema], found at [pageUrl]. */
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
