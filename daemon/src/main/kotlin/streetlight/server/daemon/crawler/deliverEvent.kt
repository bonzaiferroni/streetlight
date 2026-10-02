package streetlight.server.daemon.crawler

import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import kampfire.model.toDataOr
import kampfire.utils.fuzzyMatches
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import streetlight.model.data.EventEdit
import streetlight.model.data.EventFeed
import streetlight.model.data.GeneralEventFeed
import streetlight.model.data.Location
import streetlight.server.daemon.agent.parseLocalDateTime
import streetlight.server.routes.createEvent
import kotlin.time.Clock

/**
 * Delivers the event of [feed] made of what the feed showed, [feedEvent], and what its page showed, [pageEvent]:
 * created at the location it names, unless it lacks a title, a start ahead, or a location, or duplicates one.
 */
suspend fun Crawler.deliverEvent(feed: EventFeed, feedEvent: PropertyMap?, pageEvent: PropertyMap?, tracker: ParseTracker) {
    val event = mergeEvent(feedEvent, pageEvent, feed.timeZoneId) ?: return
    tracker.trackFoundRecord()
    val feedEdit = event.toEventEdit(feed.timeZoneId, feed.parseMode, tracker)
        .let { if ((feed as? GeneralEventFeed)?.isRsvp == true) it.withRsvp() else it.withSourceNote(feed.initialUrl) }
    val title = feedEdit.title ?: return tracker.trackUnnamedRecord(event)
    val feedStart = feedEdit.startsAt ?: return tracker.trackUnparsedEvent(event)
    if (feedStart < Clock.System.now()) return tracker.trackPastEvent()
    val location = spawner.locateEvent(event, feed, tracker) ?: return tracker.trackUnlocatedEvent(event, title)
    createEventAt(event, feedEdit, location, tracker)
}

/** Creates the [event] edited as [edit] at [location], unless an event there the same day already has its title. */
internal suspend fun Crawler.createEventAt(event: PropertyMap, edit: EventEdit, location: Location, tracker: ParseTracker) {
    val title = edit.title ?: return
    val located = edit.copy(locationId = location.locationId, timeZoneId = location.timezoneId)
    val startsAt = located.startsAt ?: return
    val zone = located.timeZone ?: return
    val day = startsAt.toLocalDateTime(zone).date

    val created = dbWrite {
        val sameDay = dao.event.readEventsBetween(
            locationId = location.locationId,
            from = day.atStartOfDayIn(zone),
            until = day.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone),
        )
        sameDay.firstOrNull { it.title.fuzzyMatches(title) }?.let { return tracker.trackDuplicateRecord(event, title, it.title) }
        server.createEvent(null, located, isImageRequired = false)
    }.toDataOr { return tracker.trackFailedRecord(event, title, it) }

    if (located.image != null && created.image == null) tracker.trackFailedImage(event, title)
    tracker.trackCreatedRecord()
}

/** The event made of what its feed showed and what its page showed, the page's preferred, or null when neither did. */
private fun mergeEvent(feedEvent: PropertyMap?, pageEvent: PropertyMap?, timeZoneId: String): PropertyMap? {
    if (feedEvent == null || pageEvent == null) return pageEvent ?: feedEvent
    val (date, startTime) = startOf(pageEvent, feedEvent, timeZoneId)
    return buildMap {
        putAll(feedEvent)
        putAll(pageEvent)
        feedEvent[ParseProperty.Url]?.let { put(ParseProperty.Url, it) }
        remove(ParseProperty.Date)
        remove(ParseProperty.StartTime)
        date?.let { put(ParseProperty.Date, it) }
        startTime?.let { put(ParseProperty.StartTime, it) }
    }
}

/**
 * The date and start time text of an event, pairing the page's with the feed's: the first pair that parses in
 * [timeZoneId], the page's preferred, or the page's own when none does.
 */
private fun startOf(page: PropertyMap, feed: PropertyMap, timeZoneId: String): Pair<String?, String?> {
    val pageDate = page[ParseProperty.Date]
    val pageTime = page[ParseProperty.StartTime]
    val feedDate = feed[ParseProperty.Date]
    val feedTime = feed[ParseProperty.StartTime]
    val pairs = listOf(
        pageDate to pageTime,
        feedDate to pageTime,
        pageDate to feedTime,
        feedDate to feedTime,
    )
    return pairs.firstOrNull { (date, time) -> startParses(date, time, timeZoneId) }
        ?: ((pageDate ?: feedDate) to (pageTime ?: feedTime))
}

private fun startParses(date: String?, time: String?, timeZoneId: String): Boolean {
    val text = listOfNotNull(date, time.takeIf { it != date }).joinToString(" ").ifBlank { return false }
    return parseLocalDateTime(text, timeZoneId) != null
}
