package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import streetlight.server.daemon.agent.FetchText
import streetlight.server.daemon.agent.LMProblem
import streetlight.server.daemon.agent.SchemaProblem
import streetlight.server.daemon.agent.parseHtmlDocument
import streetlight.model.data.FetchMode
import streetlight.model.data.Origin
import streetlight.model.data.SelectorSchema
import streetlight.model.data.toOriginId

/**
 * The document of a lead: the page at [url] on [origin], parsed as [doc] from [fetch] and registered under
 * [pageUrl], recorded by [tracker].
 */
class LeadDocument(
    val origin: Origin,
    val url: Url,
    val doc: Document,
    val fetch: FetchText,
    val pageUrl: Url,
    val tracker: PageTracker,
)

/** The step that finds the schema of [lead], stored for its origin or anew. */
typealias SchemaProvision<S> = suspend Crawler.(lead: LeadDocument) -> Outcome<S>

/**
 * The result of [block], given the document of the lead at [initialUrl] and the schema [provision] finds for it.
 * The lead is fetched in its origin's fetch mode, and again with scripting when its content is incomplete. Null
 * when the lead is not due, since its last read stopped it or it was already fetched this run, or when it could not
 * be fetched or given a schema.
 */
suspend fun <S : SelectorSchema, T> Crawler.crawlLead(
    initialUrl: Url,
    tracker: PageTracker,
    provision: SchemaProvision<S>,
    block: suspend Crawler.(lead: LeadDocument, schema: S) -> T?,
): T? {
    val link = dao.link.readLinkByAlias(initialUrl)
    if (link?.stopsFeed() == true) {
        tracker.skipped(PageState.Skipped, "Stopped by its last read: ${link.access}, ${link.content}, ${link.parseOutcome}")
        return null
    }
    if (link != null && link.fetchedAt >= startedAt) return null
    val url = link?.url ?: initialUrl
    val origin = url.toOriginId()?.let { dao.origin.readOrCreateOrigin(it) } ?: return null
    val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode

    var lead = fetchLead(origin, url, fetchMode, tracker) ?: return null
    var schema = provideSchema(lead, provision)
    if (schema == SchemaProblem.Incomplete && fetchMode == FetchMode.Basic) {
        lead = fetchLead(origin, url, FetchMode.Scripting, tracker) ?: return null
        schema = provideSchema(lead, provision)
    }
    return block(lead, schema.toDataOr { return null })
}

/**
 * The document of the page at [url] on [origin], fetched in [fetchMode] through its robots gate and registered as a
 * link, or null when it could not be fetched or parsed.
 */
private suspend fun Crawler.fetchLead(
    origin: Origin,
    url: Url,
    fetchMode: FetchMode,
    tracker: PageTracker,
): LeadDocument? {
    val fetch = getRobotGate(origin).fetchWhenOpen(url, fetchMode).toDataOr {
        logProblem(it)
        tracker.fetchFailed(it)
        return null
    }
    val doc = parseHtmlDocument(fetch.text, fetch.pageUrl).toDataOrNull(this::logProblem)
    tracker.fetched(fetchMode, fetch, doc)
    if (doc == null) return null
    val pageUrl = dao.registerFetch(origin.originId, doc, fetch)
    return LeadDocument(origin, url, doc, fetch, pageUrl, tracker)
}

/** The schema [provision] finds for [lead], with a problem logged and recorded on its tracker. */
private suspend fun <S : SelectorSchema> Crawler.provideSchema(
    lead: LeadDocument,
    provision: SchemaProvision<S>,
): Outcome<S> {
    val schema = provision(lead)
    if (schema is Problem) {
        logProblem(schema)
        if (schema == LMProblem.UsageLimit) lmUsageLimitReached = true
        lead.tracker.schemaFailed(schema)
    }
    return schema
}
