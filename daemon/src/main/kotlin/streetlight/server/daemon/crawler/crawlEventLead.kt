package streetlight.server.daemon.crawler

import kampfire.model.toDataOr
import kampfire.model.Url
import kampfire.model.toUrl
import streetlight.model.data.EventLead
import streetlight.model.data.EventSchema
import streetlight.model.data.LmSchema
import streetlight.model.data.Location
import streetlight.model.data.ParseMode
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.model.data.SchemaType
import streetlight.server.utils.readImageUrl
import kotlin.time.Clock

/**
 * Delivers the event read from the page of [lead] fetched as [document], as [schema]: at its place, found stored or
 * created first.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlEventLead(lead: EventLead, document: FetchDocument?, schema: LmSchema?) {
    val read = schema as? EventSchema ?: return
    if (document == null) return
    tracker.read(lead.initialUrl, SchemaType.EventPage)
    val event = read.toPropertyMap(document.doc, lead.initialUrl)

    tracker.recordFound()
    val title = read.name ?: return tracker.recordUnnamed(event)
    val location = placeEvent(read, event) ?: return tracker.eventUnlocated(event, title)
    val edit = event.toEventEdit(location.timezoneId, ParseMode.Partial, tracker)
    val startsAt = edit.startsAt ?: return tracker.eventUnparsed(event)
    if (startsAt < Clock.System.now()) return tracker.eventPast()
    createEventAt(event, edit, location, tracker)
}

/**
 * The location of the event [read] from its page: the place its location details find on the map, already stored or
 * created from them, its website preferred to the map's; or null when they find no place or it cannot be created.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.placeEvent(read: EventSchema, event: PropertyMap): Location? {
    val name = read.locationName ?: return null
    val area = listOfNotNull(read.locationCity, read.locationState, read.locationPostalCode)
        .joinToString(" ").ifEmpty { null }
    val website = read.locationWebsite?.toUrl()?.takeIf { it.isAbsolute }
    spawner.readStoredAt(name, read.locationAddress)?.let {
        tracker.locationMatched(event, name, it)
        return it
    }
    val (place, placedName) = spawner.findPlace(website, name, null, read.locationAddress, area)
        ?: return placeAtAddress(read.locationAddress ?: return null, area, website, event, name)
    spawner.readStored(place, placedName)?.let {
        tracker.locationMatched(event, name, it)
        return it
    }
    val details = mapOf(ParseProperty.Name to placedName)
    val created = spawner.createFrom(place, details, website).toDataOr {
        tracker.locationFailed(event, name, it)
        return null
    }
    tracker.locationSpawned(event, name, created)
    return created
}

/**
 * The location at the [address] in [area] of an event whose place, named [name], the map does not know: stored
 * there already, or created there with no name of its own.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.placeAtAddress(
    address: String,
    area: String?,
    website: Url?,
    event: PropertyMap,
    name: String,
): Location? {
    val spot = spawner.findAddress(address, area) ?: return null
    spawner.readStoredUnnamed(spot)?.let {
        tracker.locationMatched(event, name, it)
        return it
    }
    val created = spawner.createAt(spot, website).toDataOr {
        tracker.locationFailed(event, name, it)
        return null
    }
    tracker.locationSpawned(event, name, created)
    return created
}
