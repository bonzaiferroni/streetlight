package streetlight.server.daemon.crawler

import kampfire.model.toUrl
import streetlight.model.data.EventLead
import streetlight.model.data.EventSchema
import streetlight.model.data.LmSchema
import streetlight.model.data.Location
import streetlight.model.data.ParseMode
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.model.data.SchemaType
import streetlight.server.daemon.agent.LdPlace
import streetlight.server.daemon.agent.readPageLdEvent
import streetlight.server.daemon.agent.toPropertyMap
import kotlin.time.Clock

/**
 * Delivers the event read from the page of [lead] fetched as [document], as [schema], with the values its JSON-LD
 * declares laid over it: at its place, found stored or created first.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlEventLead(lead: EventLead, document: FetchDocument?, schema: LmSchema?) {
    val eventSchema = schema as? EventSchema ?: return
    if (document == null) return
    tracker.read(lead.initialUrl, SchemaType.EventPage)
    val ldEvent = document.doc.readPageLdEvent(document.servedUrl)
    val declaredEvent = ldEvent?.toPropertyMap().orEmpty()
    if (declaredEvent.isNotEmpty()) tracker.declared(lead.initialUrl)
    val event = eventSchema.toPropertyMap(document.doc, lead.initialUrl) + declaredEvent

    tracker.recordFound()
    val title = event[ParseProperty.Name] ?: return tracker.recordUnnamed(event)
    val location = placeEvent(eventSchema, ldEvent?.place, event) ?: return tracker.eventUnlocated(event, title)
    val edit = event.toEventEdit(location.timezoneId, ParseMode.Partial, tracker)
    val startsAt = edit.startsAt ?: return tracker.eventUnparsed(event)
    if (startsAt < Clock.System.now()) return tracker.eventPast()
    createEventAt(event, edit, location, tracker)
}

/**
 * The location of the event read as [schema] from its page, placed by [LocationSpawner.place] from its location
 * details, each detail of the [place] its JSON-LD declares preferred; or null when it names no place or none is found.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.placeEvent(schema: EventSchema, place: LdPlace?, event: PropertyMap): Location? {
    val name = place?.name ?: schema.locationName ?: return null
    val address = place?.street ?: schema.locationAddress
    val area = place?.area ?: listOfNotNull(schema.locationCity, schema.locationState, schema.locationPostalCode)
        .joinToString(" ").ifEmpty { null }
    val website = (place?.url ?: schema.locationWebsite)?.toUrl()?.takeIf { it.isAbsolute }
    return spawner.place(name, address, area, website, null, event, tracker)
}
