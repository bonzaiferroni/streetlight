package streetlight.server.daemon.crawler

import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.model.data.buildPropertyMap
import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import streetlight.server.daemon.agent.isPlausibleField
import streetlight.server.daemon.agent.isPlausibleProse
import streetlight.server.daemon.agent.absoluteUrl
import streetlight.server.daemon.agent.innerHtml
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.queryElement
import streetlight.server.daemon.agent.readPageLdEvent
import streetlight.server.daemon.agent.toPropertyMap
import streetlight.model.data.EventPageSchema
import streetlight.model.data.EventPage
import streetlight.model.data.SchemaType
import streetlight.model.data.LmSchema
import streetlight.server.utils.readImageUrl

/**
 * Delivers the event [schema] reads by selector from the event [lead] fetched as [document], with the values its
 * JSON-LD declares laid over it, merged with what its feed showed.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlEventPage(
    lead: EventPage,
    document: FetchDocument?,
    schema: LmSchema?,
) {
    val readEvent = (schema as? EventPageSchema)?.let { selectors -> document?.let { parsePageEvent(selectors, it.doc, it.servedUrl) } }
    val declaredEvent = document?.let { it.doc.readPageLdEvent(it.servedUrl)?.toPropertyMap() }?.takeIf { it.isNotEmpty() }
    declaredEvent?.let { tracker.declared(lead.initialUrl) }
    val pageEvent = if (readEvent == null && declaredEvent == null) null else readEvent.orEmpty() + declaredEvent.orEmpty()
    when {
        document == null || pageEvent == null -> {
            deliverEvent(lead.feed, lead.feedEvent, null, tracker)
        }
        else -> {
            tracker.read(lead.initialUrl, SchemaType.EventPage)
            log.debug { "Parsed ${document.servedUrl}: description ${pageEvent[ParseProperty.Description]?.length ?: 0} chars" }
            if (pageEvent[ParseProperty.Description] == null && pageEvent[ParseProperty.DeclaredDescription] == null) tracker.partial(lead.initialUrl, "The page had no description")
            deliverEvent(lead.feed, lead.feedEvent, pageEvent, tracker)
        }
    }
}

/** The properties of the event on the page [doc], read by [schema], found at [pageUrl], its meta image first. */
private fun parsePageEvent(
    schema: EventPageSchema,
    doc: Document,
    pageUrl: Url,
): PropertyMap {
    return buildPropertyMap {
        this[ParseProperty.Name] = doc.queryElement(schema.title) { it.isPlausibleField() }.plainText()
        this[ParseProperty.Url] = pageUrl.value
        this[ParseProperty.Image] = doc.readImageUrl()?.value ?: doc.queryElement(schema.image).absoluteUrl("src")
        this[ParseProperty.Description] = doc.queryElement(schema.description) { it.isPlausibleProse() }.innerHtml()
        this[ParseProperty.Contact] = doc.queryElement(schema.contact) { it.isPlausibleField() }.plainText()
        this[ParseProperty.Cost] = doc.queryElement(schema.cost) { it.isPlausibleField() }.plainText()
        this[ParseProperty.AgeMin] = doc.queryElement(schema.ageMin) { it.isPlausibleField() }.plainText()
        this[ParseProperty.Date] = doc.dateText(schema.date, schema.month, schema.day) { it.isPlausibleField() }
        this[ParseProperty.StartTime] = doc.queryElement(schema.startTime) { it.isPlausibleField() }.plainText()
        this[ParseProperty.EndTime] = doc.queryElement(schema.endTime) { it.isPlausibleField() }.plainText()
        this[ParseProperty.Location] = doc.queryElement(schema.location) { it.isPlausibleField() }.plainText()
        this[ParseProperty.Address] = doc.queryElement(schema.address) { it.isPlausibleField() }.plainText()
    }
}
