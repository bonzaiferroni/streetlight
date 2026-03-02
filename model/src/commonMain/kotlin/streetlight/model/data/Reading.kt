package streetlight.model.data

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable

@Serializable
data class ParseRequest(
    val url: String,
    val isImage: Boolean = false
)

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
    val time: LocalTime? = null,
    val date: LocalDate? = null,
    val location: String? = null,
    val address: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val ageMin: Int? = null,
    val contact: String? = null,
    val url: String? = null,
) {
    val startsAt: Instant? get() = if (time != null && date != null) toInstant(date, time) else null

    private fun toInstant(
        date: LocalDate,
        time: LocalTime,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Instant {
        return date
            .atTime(time)
            .toInstant(timeZone)
    }
}

fun EventParse.toEventEdit(
    sourceUrl: String?,
    sourceImageUrl: String?,
    locationId: LocationId?
): EventEdit? {
    val date = date ?: return null
    return EventEdit(
        title = name ?: "",
        locationId = locationId,
        place = Place(location ?: ""),
        imageUrl = imageUrl,
        description = description,
        url = url,
        sourceUrl = sourceUrl,
        sourceImageUrl = sourceImageUrl,
        ageMin = ageMin?.takeIf { it > 0 },
        startsAt = startsAt,
        date = date
    )
}
