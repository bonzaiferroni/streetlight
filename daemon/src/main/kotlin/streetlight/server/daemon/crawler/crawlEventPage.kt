package streetlight.server.daemon.crawler

import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
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
    val read = (schema as? EventPageSchema)?.let { selectors -> document?.let { parsePageEvent(selectors, it.doc, it.servedUrl) } }
    val declared = document?.let { it.doc.readPageLdEvent(it.servedUrl)?.toPropertyMap() }?.takeIf { it.isNotEmpty() }
    declared?.let { tracker.declared(lead.initialUrl) }
    val pageEvent = if (read == null && declared == null) null else read.orEmpty() + declared.orEmpty()
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
    return listOf(
        ParseProperty.Name to doc.queryElement(schema.title) { it.isPlausibleField() }.plainText(),
        ParseProperty.Url to pageUrl.value,
        ParseProperty.Image to (doc.readImageUrl()?.value ?: doc.queryElement(schema.image).absoluteUrl("src")),
        ParseProperty.Description to doc.queryElement(schema.description) { it.isPlausibleProse() }.innerHtml(),
        ParseProperty.Contact to doc.queryElement(schema.contact) { it.isPlausibleField() }.plainText(),
        ParseProperty.Cost to doc.queryElement(schema.cost) { it.isPlausibleField() }.plainText(),
        ParseProperty.AgeMin to doc.queryElement(schema.ageMin) { it.isPlausibleField() }.plainText(),
        ParseProperty.Date to doc.dateText(schema.date, schema.month, schema.day) { it.isPlausibleField() },
        ParseProperty.StartTime to doc.queryElement(schema.startTime) { it.isPlausibleField() }.plainText(),
        ParseProperty.EndTime to doc.queryElement(schema.endTime) { it.isPlausibleField() }.plainText(),
        ParseProperty.Location to doc.queryElement(schema.location) { it.isPlausibleField() }.plainText(),
        ParseProperty.Address to doc.queryElement(schema.address) { it.isPlausibleField() }.plainText(),
    ).mapNotNull { (property, text) -> text?.let { property to it } }.toMap()
}
