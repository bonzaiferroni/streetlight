package streetlight.server.daemon.crawler

import streetlight.model.data.RawEvent
import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import streetlight.server.daemon.agent.isPlausibleField
import streetlight.server.daemon.agent.isPlausibleProse
import streetlight.server.daemon.agent.absoluteUrl
import streetlight.server.daemon.agent.innerHtml
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.queryElement
import streetlight.model.data.EventPageSchema
import streetlight.model.data.EventPage
import streetlight.model.data.SchemaType
import streetlight.model.data.SelectorSchema
import streetlight.server.utils.readImageUrl

/** Delivers the event [selectorSchema] reads from the event [lead] fetched as [document], merged with what its feed showed. */
context(tracker: ParseTracker, pageTracker: PageTracker)
suspend fun Crawler.crawlEventPage(
    lead: EventPage,
    document: FetchDocument?,
    selectorSchema: SelectorSchema?,
) {
    val schema = selectorSchema as? EventPageSchema
    when {
        document == null || schema == null -> {
            deliverEvent(lead.feed, lead.feedEvent, null, tracker)
        }
        else -> {
            pageTracker.read(SchemaType.EventPage)
            val pageEvent = parsePageEvent(schema, document.doc, document.servedUrl)
            log.debug { "Parsed ${document.servedUrl}: description ${pageEvent.descriptionHtml?.length ?: 0} chars, cost ${pageEvent.cost}" }
            deliverEvent(lead.feed, lead.feedEvent, pageEvent, tracker)
        }
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
