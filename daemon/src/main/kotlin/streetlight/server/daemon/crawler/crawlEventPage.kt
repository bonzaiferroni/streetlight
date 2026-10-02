package streetlight.server.daemon.crawler

import streetlight.model.data.ParseProperty
import streetlight.model.data.RawEntity
import streetlight.model.data.buildRawEntity
import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import streetlight.server.daemon.agent.isPlausibleField
import streetlight.server.daemon.agent.imageUrl
import streetlight.server.daemon.agent.isPlausibleProse
import streetlight.server.daemon.agent.absoluteUrl
import streetlight.server.daemon.agent.innerHtml
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.queryElement
import streetlight.server.daemon.agent.toRawEntity
import streetlight.server.daemon.agent.readPageLdEvent
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
    val rawReadEvent = (schema as? EventPageSchema)?.let { selectors -> document?.let { parsePageEvent(selectors, it.doc, it.servedUrl, !lead.isExternalOrigin) } }
    val rawDeclaredEvent = document?.let { it.doc.readPageLdEvent(it.servedUrl, !lead.isExternalOrigin)?.toRawEntity() }?.takeIf { it.isNotEmpty() }
    rawDeclaredEvent.trackDeclaredLd(lead.initialUrl)
    val rawPageEvent = if (rawReadEvent == null && rawDeclaredEvent == null) null else rawReadEvent.orEmpty() + rawDeclaredEvent.orEmpty()

    when {
        document == null || rawPageEvent == null -> {
            deliverEvent(lead.feed, lead.rawFeedEvent, null, tracker)
        }
        else -> {
            tracker.trackReadUrl(lead.initialUrl, SchemaType.EventPage)
            log.debug { "Parsed ${document.servedUrl}: description ${rawPageEvent[ParseProperty.Description]?.length ?: 0} chars" }
            if (rawPageEvent[ParseProperty.Description] == null && rawPageEvent[ParseProperty.DeclaredDescription] == null) tracker.trackPartialUrl(lead.initialUrl, "The page had no description")
            deliverEvent(lead.feed, lead.rawFeedEvent, rawPageEvent, tracker)
        }
    }
}

/**
 * The properties of the event on the page [doc], read by [schema], found at [pageUrl], its meta image first, resolved
 * against the page when relative and [resolveIfRelative].
 */
private fun parsePageEvent(
    schema: EventPageSchema,
    doc: Document,
    pageUrl: Url,
    resolveIfRelative: Boolean,
): RawEntity {
    return buildRawEntity {
        this[ParseProperty.Name] = doc.queryElement(schema.title) { it.isPlausibleField() }.plainText()
        this[ParseProperty.Url] = pageUrl.value
        this[ParseProperty.Image] = doc.readImageUrl(resolveIfRelative)?.value ?: doc.queryElement(schema.image).imageUrl()
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
