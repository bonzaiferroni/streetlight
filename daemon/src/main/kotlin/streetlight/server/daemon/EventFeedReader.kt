package streetlight.server.daemon

import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kampfire.model.toUrl
import streetlight.agent.LMProblem
import streetlight.agent.SchemaProblem
import streetlight.agent.isPlausibleProse
import streetlight.agent.absoluteUrl
import streetlight.agent.innerHtml
import streetlight.agent.plainText
import streetlight.agent.parseHtmlDocument
import streetlight.agent.parseLocalDateTime
import streetlight.agent.queryElement
import streetlight.agent.tryQuery
import streetlight.model.data.FetchMode
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.Origin
import streetlight.model.data.SchemaType
import streetlight.model.data.toOriginId

class EventFeedReader(
    private val location: LocationConfigContent,
    private val origin: Origin,
    private val initialUrl: Url,
    private val daemon: ParseDaemon,
    private val tracker: ParseTracker,
) {
    val dao get() = daemon.dao
    val log get() = daemon.log

    private val feed get() = tracker.feed
    private val timeZoneId get() = location.location.timezoneId

    suspend fun read(): List<RawEvent>? {
        val link = dao.link.readLinkByAlias(initialUrl)
        if (link?.stopsFeed() == true) {
            feed.skipped(PageState.Skipped, "Stopped by its last read: ${link.access}, ${link.content}, ${link.parseOutcome}")
            return null
        }
        val url = link?.let {
            if (link.fetchedAt >= daemon.startedAt) return null
            link.url
        } ?: initialUrl
        val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode
        return read(url, fetchMode)
    }

    /** Reads the feed at [url] fetched in [fetchMode], and again with scripting when its content is incomplete. */
    private suspend fun read(url: Url, fetchMode: FetchMode): List<RawEvent>? {
        val gate = daemon.getRobotGate(origin)
        val fetch = gate.fetchWhenOpen(url, fetchMode).toDataOr {
            daemon.logProblem(it)
            feed.fetchFailed(it)
            return null
        }

        val doc = parseHtmlDocument(fetch.text, fetch.pageUrl).toDataOrNull(daemon::logProblem)
        feed.fetched(fetchMode, fetch, doc)
        if (doc == null) return null
        dao.registerFetch(origin.originId, doc, fetch)

        val feedSchema = daemon.mediator.feedSchema(
            url = url,
            doc = doc,
            store = OriginSchemaStore(dao, origin),
            timeZoneId = timeZoneId,
            allowLm = !daemon.lmUsageLimitReached,
            observer = feed,
        ).toDataOr {
            daemon.logProblem(it)
            if (it == LMProblem.UsageLimit) daemon.lmUsageLimitReached = true
            feed.schemaFailed(it)
            if (it == SchemaProblem.Incomplete && fetchMode == FetchMode.Basic) return read(url, FetchMode.Scripting)
            return null
        }
        val body = doc.body()
        val pageElements = feedSchema.event?.let { body.tryQuery(it).toDataOrNull(daemon::logProblem) }
        feed.collect(pageElements.orEmpty())
        if (pageElements.isNullOrEmpty()) {
            feed.noEvents(SchemaType.EventFeed)
            return null
        }
        feed.read(SchemaType.EventFeed)
        val feedUrls = setOf(url.normalize(), fetch.pageUrl.normalize())

        return pageElements.mapNotNull { element ->
            val feedEvent = RawEvent(
                title = element.queryElement(feedSchema.title).plainText(),
                url = element.queryElement(feedSchema.link).absoluteUrl("href")?.toUrl()?.normalize(),
                image = element.queryElement(feedSchema.image).absoluteUrl("src"),
                descriptionHtml = element.queryElement(feedSchema.description) { it.isPlausibleProse() }.innerHtml(),
                cost = element.queryElement(feedSchema.cost).plainText(),
                date = element.dateText(feedSchema.date, feedSchema.month, feedSchema.day),
                startTime = element.queryElement(feedSchema.time).plainText(),
            )

            val pageUrl = feedEvent.url ?: return@mapNotNull feedEvent
            if (pageUrl in feedUrls) return@mapNotNull feedEvent
            tracker.linkFound()
            if (tracker.hasPage(pageUrl)) return@mapNotNull feedEvent
            val pageLink = dao.link.readLinkByAlias(pageUrl)
            if (pageLink != null && !pageLink.wantsRead()) {
                tracker.pageKnown()
                return@mapNotNull null
            }
            if (!tracker.canReadPage()) {
                tracker.pageDeferred(pageUrl)
                return@mapNotNull null
            }
            val pageEvent = if (tracker.shouldFetch(pageUrl)) {
                val pageOrigin = pageUrl.toOriginId()?.takeIf { it != origin.originId }?.let {
                    dao.origin.readOrCreateOrigin(it)
                } ?: origin
                EventPageReader(location, pageOrigin, pageUrl, daemon, tracker.page(pageUrl)).read()
            } else {
                tracker.pageBenched(pageUrl)
                null
            }

            val (date, startTime) = startOf(pageEvent, feedEvent)
            RawEvent(
                title = pageEvent?.title ?: feedEvent.title,
                url = pageUrl,
                image = pageEvent?.image ?: feedEvent.image,
                descriptionHtml = pageEvent?.descriptionHtml ?: feedEvent.descriptionHtml,
                contact = pageEvent?.contact,
                cost = pageEvent?.cost ?: feedEvent.cost,
                ageMin = pageEvent?.ageMin,
                date = date,
                startTime = startTime,
                endTime = pageEvent?.endTime,
            )
        }
    }

    /**
     * The date and start time text of an event, pairing the page's with the feed's: the first pair that parses,
     * the page's preferred, or the page's own when none does.
     */
    private fun startOf(page: RawEvent?, feed: RawEvent): Pair<String?, String?> {
        val pairs = listOf(
            page?.date to page?.startTime,
            feed.date to page?.startTime,
            page?.date to feed.startTime,
            feed.date to feed.startTime,
        )
        return pairs.firstOrNull { (date, time) -> startParses(date, time) }
            ?: ((page?.date ?: feed.date) to (page?.startTime ?: feed.startTime))
    }

    private fun startParses(date: String?, time: String?): Boolean {
        val text = listOfNotNull(date, time.takeIf { it != date }).joinToString(" ").ifBlank { return false }
        return parseLocalDateTime(text, timeZoneId) != null
    }
}
