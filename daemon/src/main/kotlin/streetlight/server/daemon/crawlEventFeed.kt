package streetlight.server.daemon

import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kampfire.model.toUrl
import streetlight.agent.LMProblem
import streetlight.agent.SchemaParserText
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
import streetlight.model.data.EventFeedSource
import streetlight.model.data.Origin
import streetlight.model.data.SchemaType
import streetlight.model.data.toOriginId

/**
 * The events read from the feed of [source] at [initialUrl] and the event pages it links to, or null when the feed
 * is not read.
 */
suspend fun Crawler.crawlEventFeed(
    source: EventFeedSource,
    origin: Origin,
    initialUrl: Url,
    tracker: ParseTracker,
): List<RawEvent>? {
    val feed = tracker.feed
    val link = dao.link.readLinkByAlias(initialUrl)
    if (link?.stopsFeed() == true) {
        feed.skipped(PageState.Skipped, "Stopped by its last read: ${link.access}, ${link.content}, ${link.parseOutcome}")
        return null
    }
    val url = link?.let {
        if (link.fetchedAt >= startedAt) return null
        link.url
    } ?: initialUrl
    val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode
    return crawlEventFeed(source, origin, url, fetchMode, tracker)
}

/** The events of the feed at [url] fetched in [fetchMode], fetched again with scripting when its content is incomplete. */
private suspend fun Crawler.crawlEventFeed(
    source: EventFeedSource,
    origin: Origin,
    url: Url,
    fetchMode: FetchMode,
    tracker: ParseTracker,
): List<RawEvent>? {
    val feed = tracker.feed
    val gate = getRobotGate(origin)
    val fetch = gate.fetchWhenOpen(url, fetchMode).toDataOr {
        logProblem(it)
        feed.fetchFailed(it)
        return null
    }

    val doc = parseHtmlDocument(fetch.text, fetch.pageUrl).toDataOrNull(this::logProblem)
    feed.fetched(fetchMode, fetch, doc)
    if (doc == null) return null
    dao.registerFetch(origin.originId, doc, fetch)

    val feedSchema = mediator.feedSchema(
        url = url,
        doc = doc,
        store = OriginSchemaStore(dao, origin),
        timeZoneId = source.timeZoneId,
        instructions = SchemaParserText.feedSelectorsInstructions(source),
        allowLm = !lmUsageLimitReached,
        observer = feed,
    ).toDataOr {
        logProblem(it)
        if (it == LMProblem.UsageLimit) lmUsageLimitReached = true
        feed.schemaFailed(it)
        if (it == SchemaProblem.Incomplete && fetchMode == FetchMode.Basic) return crawlEventFeed(source, origin, url, FetchMode.Scripting, tracker)
        return null
    }
    val body = doc.body()
    val pageElements = feedSchema.event?.let { body.tryQuery(it).toDataOrNull(this::logProblem) }
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
            location = element.queryElement(feedSchema.eventLocation).plainText(),
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
            crawlEventPage(source, pageOrigin, pageUrl, tracker.page(pageUrl))
        } else {
            tracker.pageBenched(pageUrl)
            null
        }

        val (date, startTime) = startOf(pageEvent, feedEvent, source.timeZoneId)
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
            location = pageEvent?.location ?: feedEvent.location,
            address = pageEvent?.address,
        )
    }
}

/**
 * The date and start time text of an event, pairing the page's with the feed's: the first pair that parses in
 * [timeZoneId], the page's preferred, or the page's own when none does.
 */
private fun startOf(page: RawEvent?, feed: RawEvent, timeZoneId: String): Pair<String?, String?> {
    val pairs = listOf(
        page?.date to page?.startTime,
        feed.date to page?.startTime,
        page?.date to feed.startTime,
        feed.date to feed.startTime,
    )
    return pairs.firstOrNull { (date, time) -> startParses(date, time, timeZoneId) }
        ?: ((page?.date ?: feed.date) to (page?.startTime ?: feed.startTime))
}

private fun startParses(date: String?, time: String?, timeZoneId: String): Boolean {
    val text = listOfNotNull(date, time.takeIf { it != date }).joinToString(" ").ifBlank { return false }
    return parseLocalDateTime(text, timeZoneId) != null
}
