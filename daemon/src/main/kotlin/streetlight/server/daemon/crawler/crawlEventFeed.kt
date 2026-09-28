package streetlight.server.daemon.crawler

import streetlight.model.data.RawEvent
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
import streetlight.model.data.SelectorSchema

/**
 * Delivers the events [selectorSchema] finds in the [lead] fetched as [document], each merged with its event page when it
 * links to one worth reading.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlEventFeed(lead: EventFeed, document: FetchDocument?, selectorSchema: SelectorSchema?) {
    if (document == null) return
    val schema = selectorSchema as? EventFeedSchema ?: return
    val elements = findEventElements(document, schema, tracker.feed) ?: return
    val feedUrls = setOf(lead.initialUrl, document.servedUrl)
    elements.forEach { element ->
        val feedEvent = parseFeedEvent(element, schema)

        val pageUrl = feedEvent.url ?: return@forEach deliverEvent(lead, feedEvent, null, tracker)
        if (pageUrl in feedUrls) return@forEach deliverEvent(lead, feedEvent, null, tracker)
        tracker.linkFound()
        if (tracker.hasPage(pageUrl)) return@forEach deliverEvent(lead, feedEvent, null, tracker)
        val pageLink = dao.link.readLinkByAlias(pageUrl)
        if (pageLink != null && !pageLink.wantsRead()) return@forEach tracker.pageKnown()
        if (!tracker.canReadPage()) return@forEach tracker.pageDeferred(pageUrl)
        if (!tracker.shouldFetch(pageUrl)) {
            tracker.pageBenched(pageUrl)
            return@forEach deliverEvent(lead, feedEvent, null, tracker)
        }
        crawlLead(EventPage(pageUrl, lead, feedEvent))
    }
}

/** The event elements of the feed [document] found by [schema], recorded on [tracker], or null when there are none. */
private fun Crawler.findEventElements(document: FetchDocument, schema: EventFeedSchema, tracker: PageTracker): List<Element>? {
    val elements = schema.event?.let { document.doc.body().tryQuery(it).toDataOrNull(this::logProblem) }
    tracker.collect(elements.orEmpty())
    if (elements.isNullOrEmpty()) {
        tracker.noEvents(SchemaType.EventFeed)
        return null
    }
    tracker.read(SchemaType.EventFeed)
    return elements
}

private fun parseFeedEvent(element: Element, schema: EventFeedSchema) = RawEvent(
    title = element.queryElement(schema.title).plainText(),
    url = element.queryElement(schema.link).absoluteUrl("href")?.toUrl()?.normalize(),
    image = element.queryElement(schema.image).absoluteUrl("src"),
    descriptionHtml = element.queryElement(schema.description) { it.isPlausibleProse() }.innerHtml(),
    cost = element.queryElement(schema.cost).plainText(),
    date = element.dateText(schema.date, schema.month, schema.day),
    startTime = element.queryElement(schema.time).plainText(),
    location = element.queryElement(schema.eventLocation).plainText(),
)