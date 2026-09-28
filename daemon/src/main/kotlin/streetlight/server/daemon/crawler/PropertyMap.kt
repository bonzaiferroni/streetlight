package streetlight.server.daemon.crawler

import kampfire.api.toMarkdown
import kampfire.model.Url
import kampfire.model.toUrl
import koala.Image
import streetlight.model.data.EventEdit
import streetlight.model.data.LocationEdit
import streetlight.model.data.ParseMode
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.server.daemon.agent.parseLocalDateTime
import streetlight.server.daemon.agent.parseTimeFromText

/**
 * The edit of the event these properties describe, its start read in [timeZoneId], its description shortened unless
 * [parseMode] is [ParseMode.Full].
 */
fun PropertyMap.toEventEdit(timeZoneId: String?, parseMode: ParseMode, tracker: ParseTracker): EventEdit {
    val url = this[ParseProperty.Url]?.toUrl()
    val date = this[ParseProperty.Date]
    val startTime = this[ParseProperty.StartTime]

    val dateTimeText = listOfNotNull(date, startTime.takeIf { it != date })
        .joinToString(" ")
        .takeIf { it.isNotBlank() }

    val description = this[ParseProperty.Description]?.let { htmlToMarkdown(it) }?.value?.let { full ->
        if (parseMode == ParseMode.Full) return@let full
        shortenDescription(full, url).also { if (it != full) tracker.descriptionShortened() }
    }
    val start = dateTimeText?.let { parseLocalDateTime(it, timeZoneId) }
    val end = this[ParseProperty.EndTime]?.let { parseTimeFromText(it) }

    val body = listOfNotNull(
        description,
    ).joinToString("\n\n").takeIf { it.isNotBlank() }

    return EventEdit(
        title = this[ParseProperty.Name]?.withoutBracketNotes()?.takeIf { it.isNotBlank() },
        description = body?.toMarkdown(),
        contact = this[ParseProperty.Contact], // td: gather phone/email/social media separately
        website = url,
        image = this[ParseProperty.Image]?.let { Image(it.toUrl()) },
        date = start?.date,
        startTime = start?.time,
        endTime = end,
        timeZoneId = timeZoneId,
        // td: parse ageMin
    )
}

/** The edit of the location these properties describe, its homepage at [website]. */
fun PropertyMap.toLocationEdit(website: Url): LocationEdit = LocationEdit(
    name = this[ParseProperty.Name]?.takeIf { it.isNotBlank() },
    description = this[ParseProperty.Description]?.let { htmlToMarkdown(it) }?.value?.toMarkdown(),
    address = this[ParseProperty.Address],
    website = website,
    eventsUrl = this[ParseProperty.EventsLink]?.toUrl(),
    image = this[ParseProperty.Image]?.let { Image(it.toUrl()) },
)
