package streetlight.server.daemon.crawler

import kampfire.api.Markdown
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
import streetlight.model.data.RawEntity
import streetlight.model.data.buildRawEntity
import streetlight.server.daemon.agent.parseLocalDateTime
import streetlight.server.daemon.agent.parseTimeRange
import streetlight.server.utils.readImageUrl

/**
 * The edit of the event these properties describe, its start read in [timeZoneId], its description shortened unless
 * [parseMode] is [ParseMode.Full] or the page declared it.
 */
fun RawEntity.toEventEdit(timeZoneId: String?, parseMode: ParseMode, tracker: ParseTracker): EventEdit {
    val url = this[ParseProperty.Url]?.toUrl()
    val date = this[ParseProperty.Date]
    val startTime = this[ParseProperty.StartTime]

    val dateTimeText = listOfNotNull(date, startTime.takeIf { it != date })
        .joinToString(" ")
        .takeIf { it.isNotBlank() }

    val ldDescription = this[ParseProperty.LdDescription]?.let { htmlToMarkdown(it) }?.value
    val description = ldDescription ?: this[ParseProperty.Description]?.let { htmlToMarkdown(it) }?.value?.let { full ->
        if (parseMode == ParseMode.Full) return@let full
        shortenDescription(full, url).also { if (it != full) tracker.trackShortenedDescription() }
    }
    val start = dateTimeText?.let { parseLocalDateTime(it, timeZoneId) }
    val end = dateTimeText?.let { parseTimeRange(it)?.end }
        ?: this[ParseProperty.EndTime]?.takeIf { it != startTime && it != date }
            ?.let { parseTimeRange(it) }?.let { it.end ?: it.start }

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
        image = this[ParseProperty.Image]?.toUrl()?.takeIf { it.isAbsolute }?.let { Image(it) },
        date = start?.date,
        startTime = start?.time,
        endTime = end,
        timeZoneId = timeZoneId,
        // td: parse ageMin
    )
}

/** The edit of the location these properties describe, its homepage at [website] when known. */
fun RawEntity.toLocationEdit(website: Url?): LocationEdit = LocationEdit(
    name = this[ParseProperty.Name]?.takeIf { it.isNotBlank() },
    description = this[ParseProperty.Description]?.let { htmlToMarkdown(it) }?.value?.toMarkdown()
        ?.let { description -> website?.let { description.withNote(sourceNote(it)) } ?: description },
    address = this[ParseProperty.Address],
    website = website,
    eventsUrl = this[ParseProperty.EventsLink]?.toUrl(),
    extraLinks = this[ParseProperty.Menu]?.let { listOf(ExtraLink("menu", it.toUrl())) },
    image = this[ParseProperty.Image]?.toUrl()?.takeIf { it.isAbsolute }?.let { Image(it) },
)

/** This edit with an RSVP link to its website and a note that it may be required, or this edit when it has no website. */
fun EventEdit.withRsvp(): EventEdit {
    val url = website ?: return this
    return copy(
        links = links.orEmpty() + ExtraLink("RSVP", url),
        description = description.withNote("This event may require that you [RSVP](${url.value})."),
    )
}

/** This edit with a note that it was gathered automatically, linking its website, or [sourceUrl] when it has none. */
fun EventEdit.withSourceNote(sourceUrl: Url): EventEdit =
    copy(description = description.withNote(sourceNote(website ?: sourceUrl)))

/** This description with [note] as a paragraph of its own at its end. */
fun Markdown?.withNote(note: String): Markdown = listOfNotNull(this?.value, note).joinToString("\n\n").toMarkdown()

/** This description without the source or RSVP note [withNote] added at its end, or null when nothing else remains. */
fun Markdown.withoutNote(): Markdown? {
    val start = noteOpenings.mapNotNull { opening -> value.lastIndexOf(opening).takeIf { it >= 0 } }.minOrNull()
        ?: return this
    return value.take(start).replace(trailingSpace, "").takeIf { it.isNotEmpty() }?.toMarkdown()
}

/** The note that a record was gathered automatically, linking its [source]. */
fun sourceNote(source: Url) =
    "*This information was automatically gathered, please check* [the source](${source.value}) *for updates.*"

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

/** The properties of the event this schema read from the page [doc] at [url], an event lead's, the page's meta image first. */
fun EventRead.parseEvent(doc: Document, url: Url): RawEntity = buildRawEntity {
    this[ParseProperty.Name] = name
    this[ParseProperty.Url] = url.value
    this[ParseProperty.Image] = doc.readImageUrl(resolveIfRelative = false)?.value ?: imageUrl
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

private val noteOpenings = listOf("*This information was automatically gathered", "This event may require that you [RSVP]")
private val trailingSpace = Regex("""(\s|\\n)+$""")
