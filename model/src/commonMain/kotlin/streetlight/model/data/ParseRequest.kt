package streetlight.model.data

import kampfire.api.toMarkdown
import kampfire.model.Url
import kampfire.model.toUrl
import koala.toImage
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A request to read a record from a page. */
@Serializable
sealed interface ParseRequest {
    val url: Url
}

/** A request to read the page at a URL. */
@Serializable
data class UrlParseRequest(
    override val url: Url
): ParseRequest

/** A request to read a page from its HTML, already fetched. */
@Serializable
data class HtmlParseRequest(
    override val url: Url,
    val html: String,
): ParseRequest

/** A request to read a record from an image, such as a flyer. */
@Serializable
data class ImageParseRequest(
    override val url: Url,
): ParseRequest

/** The location and events read from a page. */
@Serializable
data class MultiEventParseResult(
    val hasContent: Boolean? = null,
    val location: Location? = null,
    val locationEdit: LocationEdit? = null,
    val events: List<EventEdit>
)

/** What a language model read from a page, not yet checked. */
@Serializable
data class ColdParse(
    val hasContent: Boolean? = null,
    val location: LocationParse? = null,
    val events: List<EventParse>? = null,
)

/** The events a language model read from a page. */
@Serializable
data class MultiEventParse(
    val hasContent: Boolean? = null,
    val events: List<EventParse>? = null,
)

/** The events read from a page, as edits. */
@Serializable
data class MultiEventParseResponse(
    val hasContent: Boolean? = null,
    val events: List<EventEdit>? = null,
)

/** The event a language model read from a page. */
@Serializable
data class SingleEventParse(
    val hasContent: Boolean? = null,
    val event: EventParse? = null,
)

/** A location as a language model read it from a page. */
@Serializable
data class LocationParse(
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val url: String? = null,
    val eventsUrl: String? = null,
    val aboutUrl: String? = null,
    val menuUrl: String? = null,
    val imageUrl: String? = null,
)

/** An event as a language model read it from a page. */
@Serializable
data class EventParse(
    val name: String? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val date: LocalDate? = null,
    val location: String? = null,
    val address: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val ageMin: Int? = null,
    val cost: String? = null,
    val contact: String? = null,
    val url: String? = null,
) {
    val startsAt: Instant? get() = if (startTime != null && date != null) toInstant(date, startTime) else null

    private fun toInstant(
        date: LocalDate,
        time: LocalTime,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ) = date.atTime(time).toInstant(timeZone)
}

/** An edit of a new event from what was read. */
fun EventParse.toEventEdit(
    locationId: LocationId?
) = EventEdit(
    title = name ?: "",
    locationId = locationId,
    image = imageUrl?.toImage(),
    description = description?.toMarkdown(),
    ageMin = ageMin?.takeIf { it > 0 },
    cost = floatUSDOf(cost),
    website = url?.toUrl(),
    startTime = startTime,
    endTime = endTime,
    date = date
)

private fun floatUSDOf(value: String?): Float? {
    val value = value ?: return null
    if (value.trim().lowercase() == "free") return 0f

    val cleaned = buildString {
        for (ch in value) {
            if (ch.isDigit() || ch == '.') append(ch)
        }
    }

    return cleaned.toFloatOrNull()
}