package streetlight.server.daemon.crawler

import kampfire.model.toDataOr
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
 * Delivers the event the LM read from the page of [lead] fetched as [document], as [schema]: at its place, found
 * stored or created first.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlEventLead(lead: EventLead, document: FetchDocument?, schema: LmSchema?) {
    val read = schema as? EventSchema ?: return
    if (document == null) return
    tracker.read(lead.initialUrl, SchemaType.EventPage)
    val event = listOf(
        ParseProperty.Name to read.name,
        ParseProperty.Url to lead.initialUrl.value,
        ParseProperty.Image to (document.doc.readImageUrl()?.value ?: read.imageUrl),
        ParseProperty.Description to read.description,
        ParseProperty.Contact to read.contact,
        ParseProperty.Cost to read.cost,
        ParseProperty.AgeMin to read.ageMin,
        ParseProperty.Date to read.date,
        ParseProperty.StartTime to read.startTime,
        ParseProperty.EndTime to read.endTime,
        ParseProperty.Location to read.locationName,
        ParseProperty.Address to read.locationAddress,
    ).mapNotNull { (property, text) -> text?.takeIf { it.isNotBlank() }?.let { property to it } }.toMap()

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
    val area = listOfNotNull(read.locationCity, read.locationState, read.locationPostalCode).joinToString(" ")
    val where = listOfNotNull(read.locationAddress, area.ifEmpty { null }).joinToString(", ").ifEmpty { null }
    val website = read.locationWebsite?.toUrl()?.takeIf { it.isAbsolute }
    val (place, placedName) = spawner.findPlace(website, name, null, where) ?: return null
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
