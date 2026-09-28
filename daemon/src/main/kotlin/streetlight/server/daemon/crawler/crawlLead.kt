package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Outcome
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
import streetlight.model.data.Lead
import streetlight.model.data.Origin
import streetlight.model.data.SelectorSchema
import streetlight.model.data.toOriginId
import streetlight.server.daemon.agent.SchemaParserText
import kotlin.time.Instant

/**
 * The document of a fetched page: asked for at [lead] on [origin], served from [servedUrl] at [fetchedAt] and
 * parsed as [doc].
 */
class FetchDocument(
    val lead: Lead,
    val origin: Origin,
    val servedUrl: Url,
    val doc: Document,
    val fetchedAt: Instant,
)

/**
 * Reads [lead]: fetches it in its origin's fetch mode, finds the schema its kind of page wants, fetches it again with
 * scripting when its content is incomplete, and hands the document and schema to the service for its kind. A lead
 * that is not due, since its last read stopped it or it was already fetched this run, or that cannot be fetched or
 * given a schema, passes on only what it already carries.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlLead(lead: Lead) {
    val pageTracker = when (lead) {
        is EventFeed -> tracker.feed
        is EventPage -> tracker.page(lead.initialUrl)
    }

    with(pageTracker) {
        val link = dao.link.readLinkByAlias(lead.initialUrl)
        if (link?.stopsFeed() == true) {
            pageTracker.skipped(PageState.Skipped, "Stopped by its last read: ${link.access}, ${link.content}, ${link.parseOutcome}")
            return
        }
        if (link != null && link.fetchedAt >= startedAt) return
        val origin = lead.initialUrl.toOriginId()?.let { dao.origin.readOrCreateOrigin(it) } ?: return
        val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode

        var document = fetchDocument(lead, origin, fetchMode)
        var schemaOutcome = document?.let { provideCrawl(lead, it) }
        if (schemaOutcome == SchemaProblem.Incomplete && fetchMode == FetchMode.Basic) {
            document = fetchDocument(lead, origin, FetchMode.Scripting)
            schemaOutcome = document?.let { provideCrawl(lead, it) }
        }

        document?.let { dao.registerFetch(it) }

        val schema = schemaOutcome?.toDataOrNull()
        when(lead) {
            is EventFeed -> crawlEventFeed(lead, document, schema)
            is EventPage -> crawlEventPage(lead, document, schema)
        }
    }
}

/**
 * The document of the page at [url] on [origin], fetched in [fetchMode] through its robots gate and registered as a
 * link, or null when it could not be fetched or parsed.
 */
private suspend fun Crawler.fetchDocument(
    lead: Lead,
    origin: Origin,
    fetchMode: FetchMode,
): FetchDocument? {
    val fetch = fetcher.fetch(lead.initialUrl, origin, fetchMode).toDataOr {
        logProblem(it)
        return null
    }
    val doc = parseHtmlDocument(fetch.text, fetch.servedUrl).toDataOrNull(this::logProblem) ?: return null
    return FetchDocument(lead, origin, fetch.servedUrl.normalize(), doc, fetch.fetchedAt)
}

/** The schema of [document] that its kind of [lead] wants, with a problem logged and recorded on [tracker]. */
context(tracker: PageTracker)
private suspend fun Crawler.provideCrawl(
    lead: Lead,
    document: FetchDocument,
): Outcome<SelectorSchema> {
    val outcome = when (lead) {
        is EventFeed -> provideFeedSchema(lead, document)
        is EventPage -> providePageSchema(lead, document)
    }

    if (outcome is Problem) {
        logProblem(outcome)
        tracker.schemaFailed(outcome)
    }

    return outcome
}

/**
 * The schema of the feed [document], stored for its origin or asked of the LM with the instructions for [feed], its
 * requests recorded on [tracker].
 */
context(tracker: PageTracker)
internal suspend fun Crawler.provideFeedSchema(
    feed: EventFeed,
    document: FetchDocument,
): Outcome<EventFeedSchema> = mediator.feedSchema(
    url = document.servedUrl,
    doc = document.doc,
    origin = document.origin,
    timeZoneId = feed.timeZoneId,
    instructions = SchemaParserText.feedSelectorsInstructions(feed),
    observer = tracker,
)

/**
 * The schema of the event page [document], stored for its origin or asked of the LM, its requests recorded on
 * [tracker] and its structured data logged.
 */
context(tracker: PageTracker)
internal suspend fun Crawler.providePageSchema(
    page: EventPage,
    document: FetchDocument,
): Outcome<EventPageSchema> = mediator.pageSchema(
    url = document.servedUrl,
    doc = document.doc,
    origin = document.origin,
    timeZoneId = page.feed.timeZoneId,
)
