package streetlight.server.daemon.crawler

import streetlight.model.data.ParseProperty
import streetlight.model.data.RawEntity
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
 * Delivers the event of [lead] made of what the feed showed, [rawFeedEvent], and what its page showed, [rawPageEvent]:
 * created at the location it names, unless it lacks a title, a start ahead, or a location, or duplicates one.
 */
suspend fun Crawler.deliverEvent(lead: EventFeed, rawFeedEvent: RawEntity?, rawPageEvent: RawEntity?, tracker: ParseTracker) {
    val rawEvent = mergeEvent(rawFeedEvent, rawPageEvent, lead.timeZoneId) ?: return
    tracker.trackFoundRecord()
    val initialEvent = rawEvent.toEventEdit(lead.timeZoneId, lead.parseMode, tracker)
    val title = initialEvent.title ?: return tracker.trackUnnamedRecord(rawEvent)
    val startsAt = initialEvent.startsAt ?: return tracker.trackUnparsedEvent(rawEvent)
    if (startsAt < Clock.System.now()) return tracker.trackPastEvent()
    val location = spawner.locateEvent(rawEvent, lead, tracker) ?: return tracker.trackUnlocatedEvent(rawEvent, title)

    val event = classifier.classifyEvent(initialEvent, rawEvent[ParseProperty.DeclaredType]) {
        tracker.trackClassifiedEvent(rawEvent, it)
    }
        .let { if ((lead as? GeneralEventFeed)?.isRsvp == true) it.withRsvp() else it.withSourceNote(lead.initialUrl) }
    createEventAt(rawEvent, event, location, tracker)
}

/** Creates the [rawEvent] edited as [edit] at [location], unless an event there the same day already has its title. */
internal suspend fun Crawler.createEventAt(rawEvent: RawEntity, edit: EventEdit, location: Location, tracker: ParseTracker) {
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
        sameDay.firstOrNull { it.title.fuzzyMatches(title) }?.let { return tracker.trackDuplicateRecord(rawEvent, title, it.title) }
        server.createEvent(null, located, isImageRequired = false)
    }.toDataOr { return tracker.trackFailedRecord(rawEvent, title, it) }

    if (located.image != null && created.image == null) tracker.trackFailedImage(rawEvent, title)
    tracker.trackCreatedRecord()
}

/** The event made of what its feed showed and what its page showed, the page's preferred, or null when neither did. */
private fun mergeEvent(rawFeedEvent: RawEntity?, rawPageEvent: RawEntity?, timeZoneId: String): RawEntity? {
    if (rawFeedEvent == null || rawPageEvent == null) return rawPageEvent ?: rawFeedEvent
    val (date, startTime) = startOf(rawPageEvent, rawFeedEvent, timeZoneId)
    return buildMap {
        putAll(rawFeedEvent)
        putAll(rawPageEvent)
        rawFeedEvent[ParseProperty.Url]?.let { put(ParseProperty.Url, it) }
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
private fun startOf(rawPageEvent: RawEntity, rawFeedEvent: RawEntity, timeZoneId: String): Pair<String?, String?> {
    val pageDate = rawPageEvent[ParseProperty.Date]
    val pageTime = rawPageEvent[ParseProperty.StartTime]
    val feedDate = rawFeedEvent[ParseProperty.Date]
    val feedTime = rawFeedEvent[ParseProperty.StartTime]
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
