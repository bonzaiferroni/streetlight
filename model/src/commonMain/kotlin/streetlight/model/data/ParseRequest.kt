package streetlight.model.data

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toDeprecatedInstant
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable

@Serializable
sealed interface ParseRequest {
    val url: String
}

@Serializable
data class UrlParseRequest(
    override val url: String
): ParseRequest

@Serializable
data class HtmlParseRequest(
    override val url: String,
    val html: String,
): ParseRequest

@Serializable
data class ImageParseRequest(
    override val url: String,
): ParseRequest

@Serializable
data class MultiEventParseResult(
    val hasContent: Boolean? = null,
    val location: Location? = null,
    val locationEdit: LocationEdit? = null,
    val events: List<EventEdit>
)

@Serializable
data class ColdParse(
    val hasContent: Boolean? = null,
    val location: LocationParse? = null,
    val events: List<EventParse>? = null,
)

@Serializable
data class MultiEventParse(
    val hasContent: Boolean? = null,
    val events: List<EventParse>? = null,
)

@Serializable
data class MultiEventParseResponse(
    val hasContent: Boolean? = null,
    val events: List<EventEdit>? = null,
)

@Serializable
data class SingleEventParse(
    val hasContent: Boolean? = null,
    val event: EventParse? = null,
)

@Serializable
data class SingleEventParseResponse(
    val hasContent: Boolean? = null,
    val event: EventEdit? = null,
)

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

@Serializable
data class LocationParseResult(
    val location: Location?,
    val edit: LocationEdit?,
)

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
    ) = date.atTime(time).toInstant(timeZone).toDeprecatedInstant()
}

fun EventParse.toEventEdit(
    sourceUrl: String?,
    sourceImageUrl: String?,
    locationId: LocationId?
) = EventEdit(
    title = name ?: "",
    locationId = locationId,
    imageUrl = imageUrl,
    description = description,
    ageMin = ageMin?.takeIf { it > 0 },
    cost = floatUSDOf(cost),
    link = url,
    sourceUrl = sourceUrl,
    sourceImageUrl = sourceImageUrl,
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