package streetlight.server.daemon.crawler

import kampfire.model.toUrl
import streetlight.model.data.EventLead
import streetlight.model.data.EventRead
import streetlight.model.data.LmSchema
import streetlight.model.data.Location
import streetlight.model.data.ParseMode
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.model.data.SchemaType
import streetlight.server.daemon.agent.LdPlace
import streetlight.server.daemon.agent.toPropertyMap
import streetlight.server.daemon.agent.readPageLdEvent
import streetlight.server.daemon.agent.areaOf
import kotlin.time.Clock

/**
 * Delivers the event read from the page of [lead] fetched as [document], as [schema], with the values its JSON-LD
 * declares laid over it: at its place, found stored or created first.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlEventLead(lead: EventLead, document: FetchDocument?, schema: LmSchema?) {
    val eventRead = schema as? EventRead ?: return
    if (document == null) return
    tracker.trackReadUrl(lead.initialUrl, SchemaType.EventPage)

    val ldEvent = document.doc.readPageLdEvent(document.servedUrl, !lead.isExternalOrigin)
    val declaredEvent = ldEvent?.toPropertyMap().orEmpty()
    declaredEvent.trackDeclaredLd(lead.initialUrl)
    val event = eventRead.parseEvent(document.doc, lead.initialUrl) + declaredEvent

    tracker.trackFoundRecord()
    val title = event[ParseProperty.Name] ?: return tracker.trackUnnamedRecord(event)
    val location = placeLeadEvent(eventRead, ldEvent?.place, event) ?: return tracker.trackUnlocatedEvent(event, title)
    val edit = event.toEventEdit(location.timezoneId, ParseMode.Partial, tracker)
    val startsAt = edit.startsAt ?: return tracker.trackUnparsedEvent(event)
    if (startsAt < Clock.System.now()) return tracker.trackPastEvent()
    createEventAt(event, edit, location, tracker)
}

/**
 * The location of the event read as [schema] from its page, placed by [LocationSpawner.placeEvent] from its location
 * details, each detail of the [place] its JSON-LD declares preferred; or null when it names no place or none is found.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.placeLeadEvent(schema: EventRead, place: LdPlace?, event: PropertyMap): Location? {
    val name = place?.name ?: schema.locationName ?: return null
    val address = place?.street ?: schema.locationAddress
    val area = place?.area ?: areaOf(schema.locationCity, schema.locationState, schema.locationPostalCode)
    val region = place?.region ?: schema.locationState
    val website = (place?.url ?: schema.locationWebsite)?.toUrl()?.takeIf { it.isAbsolute }
    return spawner.placeEvent(name, address, area, region, website, null, event, tracker)
}
