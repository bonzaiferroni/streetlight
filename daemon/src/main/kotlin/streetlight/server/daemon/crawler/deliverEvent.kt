package streetlight.server.daemon.crawler

import streetlight.model.data.RawEvent
import kampfire.api.toMarkdown
import kampfire.model.toDataOr
import kampfire.model.toUrl
import kampfire.utils.fuzzyMatches
import koala.Image
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import streetlight.model.data.EventEdit
import streetlight.model.data.EventFeed
import streetlight.model.data.ParseMode
import streetlight.server.daemon.agent.parseLocalDateTime
import streetlight.server.daemon.agent.parseTimeFromText
import streetlight.server.routes.createEvent
import kotlin.time.Clock

/**
 * Delivers the event of [feed] made of what the feed showed, [feedEvent], and what its page showed, [pageEvent]:
 * created at the location it names, unless it lacks a title, a start ahead, or a location, or duplicates one.
 */
suspend fun Crawler.deliverEvent(feed: EventFeed, feedEvent: RawEvent?, pageEvent: RawEvent?, tracker: ParseTracker) {
    val event = mergeEvent(feedEvent, pageEvent, feed.timeZoneId) ?: return
    tracker.eventFound()
    val feedEdit = event.toEventEdit(feed.timeZoneId, feed.parseMode, tracker)
    val title = feedEdit.title ?: return tracker.eventUntitled(event)
    val feedStart = feedEdit.startsAt ?: return tracker.eventUnparsed(event)
    if (feedStart < Clock.System.now()) return tracker.eventPast()
    val location = spawner.locate(event, feed, tracker) ?: return tracker.eventUnlocated(event, title)
    val edit = feedEdit.copy(locationId = location.locationId, timeZoneId = location.timezoneId)
    val startsAt = edit.startsAt ?: return
    val zone = edit.timeZone ?: return
    val day = startsAt.toLocalDateTime(zone).date
    val sameDay = dao.event.readEventsBetween(
        locationId = location.locationId,
        from = day.atStartOfDayIn(zone),
        until = day.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone),
    )
    sameDay.firstOrNull { it.title.fuzzyMatches(title) }?.let { return tracker.eventDuplicate(title, it.title) }
    val created = server.createEvent(null, edit, isImageRequired = false).toDataOr { return tracker.eventFailed(title, it) }
    if (edit.image != null && created.image == null) tracker.imageFailed(title)
    tracker.eventCreated()
}

/** The edit of this event with its start read in [timeZoneId], its description shortened unless [parseMode] is [ParseMode.Full]. */
private fun RawEvent.toEventEdit(timeZoneId: String?, parseMode: ParseMode, tracker: ParseTracker): EventEdit {

    val dateTimeText = listOfNotNull(date, startTime.takeIf { it != date })
        .joinToString(" ")
        .takeIf { it.isNotBlank() }

    val description = descriptionHtml?.let { htmlToMarkdown(it) }?.value?.let { full ->
        if (parseMode == ParseMode.Full) return@let full
        shortenDescription(full, url).also { if (it != full) tracker.descriptionShortened() }
    }
    val start = dateTimeText?.let { parseLocalDateTime(it, timeZoneId) }
    val end = endTime?.let { parseTimeFromText(it) }

    val body = listOfNotNull(
        description,
    ).joinToString("\n\n").takeIf { it.isNotBlank() }

    return EventEdit(
        title = title?.withoutBracketNotes()?.takeIf { it.isNotBlank() },
        description = body?.toMarkdown(),
        contact = contact, // td: gather phone/email/social media separately
        website = url,
        image = image?.let { Image(it.toUrl()) },
        date = start?.date,
        startTime = start?.time,
        endTime = end,
        timeZoneId = timeZoneId,
        // td: parse ageMin
    )
}

/** The event made of what its feed showed and what its page showed, the page's preferred, or null when neither did. */
private fun mergeEvent(feedEvent: RawEvent?, pageEvent: RawEvent?, timeZoneId: String): RawEvent? {
    if (feedEvent == null || pageEvent == null) return pageEvent ?: feedEvent
    val (date, startTime) = startOf(pageEvent, feedEvent, timeZoneId)
    return RawEvent(
        title = pageEvent.title ?: feedEvent.title,
        url = feedEvent.url ?: pageEvent.url,
        image = pageEvent.image ?: feedEvent.image,
        descriptionHtml = pageEvent.descriptionHtml ?: feedEvent.descriptionHtml,
        contact = pageEvent.contact,
        cost = pageEvent.cost ?: feedEvent.cost,
        ageMin = pageEvent.ageMin,
        date = date,
        startTime = startTime,
        endTime = pageEvent.endTime,
        location = pageEvent.location ?: feedEvent.location,
        address = pageEvent.address,
    )
}

/**
 * The date and start time text of an event, pairing the page's with the feed's: the first pair that parses in
 * [timeZoneId], the page's preferred, or the page's own when none does.
 */
private fun startOf(page: RawEvent?, feed: RawEvent, timeZoneId: String): Pair<String?, String?> {
    val pairs = listOf(
        page?.date to page?.startTime,
        feed.date to page?.startTime,
        page?.date to feed.startTime,
        feed.date to feed.startTime,
    )
    return pairs.firstOrNull { (date, time) -> startParses(date, time, timeZoneId) }
        ?: ((page?.date ?: feed.date) to (page?.startTime ?: feed.startTime))
}

private fun startParses(date: String?, time: String?, timeZoneId: String): Boolean {
    val text = listOfNotNull(date, time.takeIf { it != date }).joinToString(" ").ifBlank { return false }
    return parseLocalDateTime(text, timeZoneId) != null
}
