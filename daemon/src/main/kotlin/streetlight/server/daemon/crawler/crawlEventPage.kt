package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import kampfire.model.Outcome
import streetlight.server.daemon.agent.isPlausibleField
import streetlight.server.daemon.agent.isPlausibleProse
import streetlight.server.daemon.agent.absoluteUrl
import streetlight.server.daemon.agent.innerHtml
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.queryElement
import streetlight.model.data.EventPageSchema
import streetlight.model.data.EventFeed
import streetlight.model.data.SchemaType
import streetlight.server.utils.readImageUrl

/** The event read from its page at [initialUrl], found in the feed of [source], or null when it is not read. */
suspend fun Crawler.crawlEventPage(source: EventFeed, initialUrl: Url, tracker: PageTracker): RawEvent? =
    crawlLead(initialUrl, tracker, { provisionPageSchema(source, it) }) { page, schema ->
        tracker.read(SchemaType.EventPage)
        parsePageEvent(schema, page.doc, page.pageUrl).also {
            log.debug { "Parsed ${page.fetch.pageUrl}: description ${it.descriptionHtml?.length ?: 0} chars, cost ${it.cost}" }
        }
    }

/** The schema of the event page [lead], stored for its origin or asked of the LM, with its structured data logged. */
private suspend fun Crawler.provisionPageSchema(
    source: EventFeed,
    lead: LeadDocument,
): Outcome<EventPageSchema> {
    val report = lead.doc.readStructuredData()
    if (report.jsonLdBlocks > 0 || report.microdataEventCount > 0) {
        log.info { "structured data at ${lead.fetch.pageUrl}: blocks=${report.jsonLdBlocks} " +
                "malformed=${report.jsonLdMalformed} events=${report.eventCount} " +
                "microdataEvents=${report.microdataEventCount} types=${report.types}" }
    }
    return mediator.pageSchema(
        url = lead.fetch.pageUrl,
        doc = lead.doc,
        origin = lead.origin,
        timeZoneId = source.timeZoneId,
        allowLm = !lmUsageLimitReached,
        observer = lead.tracker,
    )
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
