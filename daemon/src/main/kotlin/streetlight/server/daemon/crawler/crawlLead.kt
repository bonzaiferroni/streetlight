package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Outcome
import streetlight.server.daemon.agent.toLocationSchema
import streetlight.server.daemon.agent.toEventSchema
import streetlight.server.daemon.agent.readLdPlaces
import streetlight.server.daemon.agent.readLdEvents
import streetlight.server.daemon.agent.pageLdEvent
import streetlight.server.daemon.agent.isCalledOff
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.normalize
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import streetlight.server.daemon.agent.SchemaProblem
import streetlight.server.daemon.agent.parseHtmlDocument
import streetlight.model.data.EventFeed
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventPage
import streetlight.model.data.EventPageSchema
import streetlight.model.data.FetchMode
import streetlight.model.data.EventLead
import streetlight.model.data.EventSchema
import streetlight.model.data.Lead
import streetlight.model.data.LmSchema
import streetlight.model.data.LocationLead
import streetlight.model.data.LocationSchema
import streetlight.model.data.Origin
import streetlight.model.data.toOriginId
import streetlight.server.daemon.agent.SchemaParserText
import kotlin.time.Instant

/**
 * The document of a fetched page: asked for at [lead] on [origin], served from [servedUrl] at [fetchedAt] in
 * [fetchMode] and parsed as [doc].
 */
class FetchDocument(
    val lead: Lead,
    val origin: Origin,
    val servedUrl: Url,
    val doc: Document,
    val fetchedAt: Instant,
    val fetchMode: FetchMode,
)

/**
 * Reads [lead]: fetches it in its origin's fetch mode, finds the schema its kind of page wants, fetches it again with
 * scripting when its content is incomplete, and hands the document and schema to the service for its kind. A lead
 * that is not due, since its last read stopped it or it was already fetched this run, or that cannot be fetched or
 * given a schema, passes on only what it already carries. A lead whose page calls its event off passes on nothing.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawl(lead: Lead) {
    val link = dao.link.readLink(lead.initialUrl)
    if (link?.stopsFeed() == true) {
        tracker.skipped(lead.initialUrl, PageState.Skipped, "Stopped by its last read: ${link.access}, ${link.content}, ${link.parseOutcome}")
        return
    }
    if (link != null && link.fetchedAt >= startedAt) return
    val origin = lead.initialUrl.toOriginId()?.let { dao.origin.readOrCreateOrigin(it) } ?: return
    val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode

    var document = fetchDocument(lead, origin, fetchMode)
    var schemaOutcome = document?.let { provideCrawl(lead, it) }
    if (schemaOutcome == SchemaProblem.ScriptingRequired && fetchMode == FetchMode.Basic) {
        document = fetchDocument(lead, origin, FetchMode.Scripting)
        schemaOutcome = document?.let { provideCrawl(lead, it) }
    }

    document?.let { dao.registerFetch(it) }
    if (schemaOutcome == SchemaProblem.CalledOff) return

    val schema = schemaOutcome?.toDataOrNull()
    when(lead) {
        is EventFeed -> crawlEventFeed(lead, document, schema)
        is EventPage -> crawlEventPage(lead, document, schema)
        is LocationLead -> crawlLocationLead(lead, document, schema)
        is EventLead -> crawlEventLead(lead, document, schema)
    }
}

/**
 * The document of the page at [url] on [origin], fetched in [fetchMode] through its robots gate and registered as a
 * link, or null when it could not be fetched or parsed.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.fetchDocument(
    lead: Lead,
    origin: Origin,
    fetchMode: FetchMode,
): FetchDocument? {
    val fetch = fetcher.fetch(lead.initialUrl, origin, fetchMode).toDataOr {
        logProblem(it)
        tracker.fetchFailed(lead.initialUrl, it)
        return null
    }
    val doc = parseHtmlDocument(fetch.text, fetch.servedUrl).toDataOrNull(this::logProblem)
    tracker.fetched(lead.initialUrl, fetchMode, fetch, doc)
    if (doc == null) return null
    return FetchDocument(lead, origin, fetch.servedUrl.normalize(), doc, fetch.fetchedAt, fetchMode)
}

/** The schema of [document] that its kind of [lead] wants, with a problem logged and recorded on [tracker]. */
context(tracker: ParseTracker)
private suspend fun Crawler.provideCrawl(
    lead: Lead,
    document: FetchDocument,
): Outcome<LmSchema> {
    val outcome = readStructured(lead, document) ?: when (lead) {
        is EventFeed -> provideFeedSchema(lead, document)
        is EventPage -> providePageSchema(lead, document)
        is LocationLead -> provideLocationSchema(lead, document)
        is EventLead -> provideEventSchema(lead, document)
    }

    if (outcome is Problem) {
        logProblem(outcome)
        tracker.schemaFailed(lead.initialUrl, outcome)
    }

    return outcome
}

/**
 * The schema of the feed [document], stored for its origin or asked of the LM with the instructions for [lead], its
 * requests recorded on [tracker].
 */
context(tracker: ParseTracker)
internal suspend fun Crawler.provideFeedSchema(
    lead: EventFeed,
    document: FetchDocument,
): Outcome<EventFeedSchema> = mediator.feedSchema(
    url = document.servedUrl,
    doc = document.doc,
    origin = document.origin,
    fetchMode = document.fetchMode,
    timeZoneId = lead.timeZoneId,
    instructions = SchemaParserText.feedSelectorsInstructions(lead),
    observer = tracker.page(lead.initialUrl),
)

/**
 * The schema of the event page [document], stored for its origin or asked of the LM, its requests recorded on
 * [tracker] and its structured data logged.
 */
context(tracker: ParseTracker)
internal suspend fun Crawler.providePageSchema(
    lead: EventPage,
    document: FetchDocument,
): Outcome<EventPageSchema> = mediator.pageSchema(
    url = document.servedUrl,
    doc = document.doc,
    origin = document.origin,
    fetchMode = document.fetchMode,
    timeZoneId = lead.feed.timeZoneId,
    observer = tracker.page(lead.initialUrl),
)

/**
 * The details of the location whose homepage is [document], read directly by the LM, its request recorded on
 * [tracker].
 */
context(tracker: ParseTracker)
internal suspend fun Crawler.provideLocationSchema(
    lead: LocationLead,
    document: FetchDocument,
): Outcome<LocationSchema> = mediator.readLocation(
    url = document.servedUrl,
    doc = document.doc,
    origin = document.origin,
    fetchMode = document.fetchMode,
    observer = tracker.page(lead.initialUrl),
)

/** The details of the event whose page is [document], read directly by the LM, its request recorded on [tracker]. */
context(tracker: ParseTracker)
internal suspend fun Crawler.provideEventSchema(
    lead: EventLead,
    document: FetchDocument,
): Outcome<EventSchema> = mediator.readEvent(
    url = document.servedUrl,
    doc = document.doc,
    origin = document.origin,
    fetchMode = document.fetchMode,
    observer = tracker.page(lead.initialUrl),
)

/**
 * The schema of [lead] read from the JSON-LD of its [document] without the LM, when the page declares what one needs,
 * or null: an event page's or event lead's own event, with a start and, for a lead, a place; a location lead's one
 * place. An event the page declares cancelled or postponed is [SchemaProblem.CalledOff].
 */
context(tracker: ParseTracker)
private fun readStructured(lead: Lead, document: FetchDocument): Outcome<LmSchema>? {
    val schema: LmSchema = when (lead) {
        is EventFeed -> return null
        is EventPage, is EventLead -> {
            val event = pageLdEvent(document.doc.readLdEvents(), document.servedUrl) ?: return null
            if (event.isCalledOff) return SchemaProblem.CalledOff
            event.toEventSchema()
                ?.takeIf { lead is EventPage || it.locationName != null || it.locationAddress != null }
                ?: return null
        }
        is LocationLead -> document.doc.readLdPlaces().distinctBy { it.name }.singleOrNull()?.toLocationSchema()
            ?: return null
    }
    tracker.readStructured(lead.initialUrl)
    return Ok(schema)
}
