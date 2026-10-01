package streetlight.server.daemon.crawler

import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.model.data.buildPropertyMap
import kampfire.model.normalize
import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.toDataOrNull
import kampfire.model.toUrl
import streetlight.server.daemon.agent.isPlausibleProse
import streetlight.server.daemon.agent.absoluteUrl
import streetlight.server.daemon.agent.innerHtml
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.queryElement
import streetlight.server.daemon.agent.tryQuery
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventFeed
import streetlight.model.data.EventPage
import streetlight.model.data.SchemaType
import streetlight.model.data.LmSchema

/**
 * Delivers the events [selectorSchema] finds in the [lead] fetched as [document], each merged with its event page when it
 * links to one worth reading.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlEventFeed(lead: EventFeed, document: FetchDocument?, selectorSchema: LmSchema?) {
    if (document == null) return
    val schema = selectorSchema as? EventFeedSchema ?: return
    val elements = findEventElements(lead, document, schema) ?: return
    val feedUrls = setOf(lead.initialUrl, document.servedUrl)

    elements.forEach { element ->
        val feedEvent = parseFeedEvent(element, schema)

        val pageUrl = feedEvent[ParseProperty.Url]?.toUrl() ?: return@forEach deliverEvent(lead, feedEvent, null, tracker)
        if (pageUrl in feedUrls) return@forEach deliverEvent(lead, feedEvent, null, tracker)
        tracker.trackFoundLink()
        if (tracker.hasPage(pageUrl)) return@forEach deliverEvent(lead, feedEvent, null, tracker)
        val pageLink = dao.link.readLink(pageUrl)
        if (pageLink != null && !pageLink.wantsRead()) return@forEach tracker.trackKnownPage()
        if (!tracker.canReadPage()) return@forEach tracker.trackSkippedUrl(pageUrl, PageState.Deferred)
        if (!tracker.shouldFetch(pageUrl)) {
            tracker.trackSkippedUrl(pageUrl, PageState.Benched)
            return@forEach deliverEvent(lead, feedEvent, null, tracker)
        }
        crawl(EventPage(pageUrl, lead, feedEvent))
    }
}

/** The event elements of the [feed]'s [document] found by [schema], recorded on its page, or null when there are none. */
context(tracker: ParseTracker)
private fun Crawler.findEventElements(feed: EventFeed, document: FetchDocument, schema: EventFeedSchema): List<Element>? {
    val elements = schema.event?.let { document.doc.tryQuery(it).toDataOrNull(this::logProblem) }
    tracker.collect(feed.initialUrl, elements.orEmpty())
    if (elements.isNullOrEmpty()) {
        tracker.trackNoEvents(feed.initialUrl, SchemaType.EventFeed)
        return null
    }
    tracker.trackReadUrl(feed.initialUrl, SchemaType.EventFeed)
    return elements
}

/** The properties of the event in [element] of a feed, read by [schema]. */
private fun parseFeedEvent(element: Element, schema: EventFeedSchema): PropertyMap = buildPropertyMap {
    this[ParseProperty.Name] = element.queryElement(schema.title).plainText()
    this[ParseProperty.Url] = element.queryElement(schema.link).absoluteUrl("href")?.toUrl()?.normalize()?.value
    this[ParseProperty.Image] = element.queryElement(schema.image).absoluteUrl("src")
    this[ParseProperty.Description] = element.queryElement(schema.description) { it.isPlausibleProse() }.innerHtml()
    this[ParseProperty.Cost] = element.queryElement(schema.cost).plainText()
    this[ParseProperty.Date] = element.dateText(schema.date, schema.month, schema.day)
    this[ParseProperty.StartTime] = element.queryElement(schema.time).plainText()
    this[ParseProperty.Location] = element.queryElement(schema.eventLocation).plainText()
}