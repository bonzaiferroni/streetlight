package streetlight.server.daemon.crawler

import kampfire.api.toMarkdown
import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import kampfire.model.toUrl
import koala.Image
import streetlight.model.data.EventEdit
import streetlight.model.data.EventRead
import streetlight.model.data.ExtraLink
import streetlight.model.data.LocationEdit
import streetlight.model.data.ParseMode
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.model.data.buildPropertyMap
import streetlight.server.daemon.agent.parseLocalDateTime
import streetlight.server.daemon.agent.parseTimeFromText
import streetlight.server.utils.readImageUrl

/**
 * The edit of the event these properties describe, its start read in [timeZoneId], its description shortened unless
 * [parseMode] is [ParseMode.Full] or the page declared it.
 */
fun PropertyMap.toEventEdit(timeZoneId: String?, parseMode: ParseMode, tracker: ParseTracker): EventEdit {
    val url = this[ParseProperty.Url]?.toUrl()
    val date = this[ParseProperty.Date]
    val startTime = this[ParseProperty.StartTime]

    val dateTimeText = listOfNotNull(date, startTime.takeIf { it != date })
        .joinToString(" ")
        .takeIf { it.isNotBlank() }

    val declared = this[ParseProperty.DeclaredDescription]?.let { htmlToMarkdown(it) }?.value
    val description = declared ?: this[ParseProperty.Description]?.let { htmlToMarkdown(it) }?.value?.let { full ->
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
        cost = this[ParseProperty.Cost]?.let { costOf(it) },
        website = url,
        links = this[ParseProperty.Tickets]?.let { listOf(ExtraLink("Tickets", it.toUrl())) },
        image = this[ParseProperty.Image]?.let { Image(it.toUrl()) },
        date = start?.date,
        startTime = start?.time,
        endTime = end,
        timeZoneId = timeZoneId,
        // td: parse ageMin
    )
}

/** The edit of the location these properties describe, its homepage at [website] when known. */
fun PropertyMap.toLocationEdit(website: Url?): LocationEdit = LocationEdit(
    name = this[ParseProperty.Name]?.takeIf { it.isNotBlank() },
    description = this[ParseProperty.Description]?.let { htmlToMarkdown(it) }?.value?.toMarkdown(),
    address = this[ParseProperty.Address],
    website = website,
    eventsUrl = this[ParseProperty.EventsLink]?.toUrl(),
    image = this[ParseProperty.Image]?.let { Image(it.toUrl()) },
)

/**
 * The cost in dollars that [text] states: 0 when it says the event is free and names no price, or its lowest dollar
 * amount, such as 15 for "$15 advance / $20 door". Null when it states neither.
 */
internal fun costOf(text: String): Float? {
    val prices = dollarAmount.findAll(text).mapNotNull { it.groupValues[1].toFloatOrNull() }.toList()
    prices.minOrNull()?.let { return it }
    return 0f.takeIf { freeWord.containsMatchIn(text) }
}

private val dollarAmount = Regex("""\$\s?(\d+(?:\.\d{1,2})?)""")
private val freeWord = Regex("""\bfree\b""", RegexOption.IGNORE_CASE)

/** The properties of the event this schema read from the page [doc] at [url], the page's meta image first. */
fun EventRead.parseEvent(doc: Document, url: Url): PropertyMap = buildPropertyMap {
    this[ParseProperty.Name] = name
    this[ParseProperty.Url] = url.value
    this[ParseProperty.Image] = doc.readImageUrl()?.value ?: imageUrl
    this[ParseProperty.Description] = description
    this[ParseProperty.Contact] = contact
    this[ParseProperty.Cost] = cost
    this[ParseProperty.AgeMin] = ageMin
    this[ParseProperty.Date] = date
    this[ParseProperty.StartTime] = startTime
    this[ParseProperty.EndTime] = endTime
    this[ParseProperty.Location] = locationName
    this[ParseProperty.Address] = locationAddress
}
