package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Outcome
import kampfire.model.Url
import kampfire.model.toDataOrNull
import kampfire.model.toUrl
import streetlight.server.daemon.agent.SchemaParserText
import streetlight.server.daemon.agent.isPlausibleProse
import streetlight.server.daemon.agent.absoluteUrl
import streetlight.server.daemon.agent.innerHtml
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.parseLocalDateTime
import streetlight.server.daemon.agent.queryElement
import streetlight.server.daemon.agent.tryQuery
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventFeed
import streetlight.model.data.SchemaType

/** Creates the events read from the feed of [source] at [initialUrl] and the event pages it links to. */
suspend fun Crawler.crawlEventFeed(
    source: EventFeed,
    initialUrl: Url,
    tracker: ParseTracker,
) {
    crawlLead(initialUrl, tracker.feed, { provisionFeedSchema(source, it) }) { feed, schema ->
        val elements = findEventElements(feed, schema) ?: return@crawlLead
        val feedUrls = setOf(feed.url.normalize(), feed.fetch.pageUrl.normalize())
        elements.forEach { element ->
            val event = crawlFeedEvent(element, schema, source, feedUrls, tracker) ?: return@forEach
            createEvent(event, source, tracker)
        }
    }
}

/** The schema of the feed [lead], stored for its origin or asked of the LM with the instructions for [source]. */
private suspend fun Crawler.provisionFeedSchema(
    source: EventFeed,
    lead: LeadDocument,
): Outcome<EventFeedSchema> = mediator.feedSchema(
    url = lead.url,
    doc = lead.doc,
    origin = lead.origin,
    timeZoneId = source.timeZoneId,
    instructions = SchemaParserText.feedSelectorsInstructions(source),
    allowLm = !lmUsageLimitReached,
    observer = lead.tracker,
)

/** The event elements of [feed] found by [schema], or null when there are none. */
private fun Crawler.findEventElements(feed: LeadDocument, schema: EventFeedSchema): List<Element>? {
    val tracker = feed.tracker
    val elements = schema.event?.let { feed.doc.body().tryQuery(it).toDataOrNull(this::logProblem) }
    tracker.collect(elements.orEmpty())
    if (elements.isNullOrEmpty()) {
        tracker.noEvents(SchemaType.EventFeed)
        return null
    }
    tracker.read(SchemaType.EventFeed)
    return elements
}

/**
 * The event read from [element] of a feed by [schema], merged with its event page when it links to one worth
 * reading, or null when its page is already known or deferred. Links back to [feedUrls] are not pages.
 */
private suspend fun Crawler.crawlFeedEvent(
    element: Element,
    schema: EventFeedSchema,
    source: EventFeed,
    feedUrls: Set<Url>,
    tracker: ParseTracker,
): RawEvent? {
    val feedEvent = RawEvent(
        title = element.queryElement(schema.title).plainText(),
        url = element.queryElement(schema.link).absoluteUrl("href")?.toUrl()?.normalize(),
        image = element.queryElement(schema.image).absoluteUrl("src"),
        descriptionHtml = element.queryElement(schema.description) { it.isPlausibleProse() }.innerHtml(),
        cost = element.queryElement(schema.cost).plainText(),
        date = element.dateText(schema.date, schema.month, schema.day),
        startTime = element.queryElement(schema.time).plainText(),
        location = element.queryElement(schema.eventLocation).plainText(),
    )

    val pageUrl = feedEvent.url ?: return feedEvent
    if (pageUrl in feedUrls) return feedEvent
    tracker.linkFound()
    if (tracker.hasPage(pageUrl)) return feedEvent
    val pageLink = dao.link.readLinkByAlias(pageUrl)
    if (pageLink != null && !pageLink.wantsRead()) {
        tracker.pageKnown()
        return null
    }
    if (!tracker.canReadPage()) {
        tracker.pageDeferred(pageUrl)
        return null
    }
    val pageEvent = if (tracker.shouldFetch(pageUrl)) {
        crawlEventPage(source, pageUrl, tracker.page(pageUrl))
    } else {
        tracker.pageBenched(pageUrl)
        null
    }

    val (date, startTime) = startOf(pageEvent, feedEvent, source.timeZoneId)
    return RawEvent(
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
