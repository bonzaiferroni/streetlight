package streetlight.model.data

import androidx.compose.runtime.Stable
import kampfire.model.LabeledEnum
import kampfire.model.UserId
import kampfire.utils.ParseHint
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import streetlight.model.utils.tomorrowNoon
import kotlin.jvm.JvmInline

@Stable
@Serializable
data class Event(
    val eventId: EventId,
    val locationId: LocationId,
    val userId: UserId,
    val currentRequestId: RequestId?,
    val title: String,
    val description: String?,
    val status: EventStatus,
    val eventType: EventType,
    val contact: String?,
    val invitation: String?,
    val ageMin: Int?,
    val visibility: Int?,
    val url: String?,
    val sourceUrl: String?,
    val sourceImageUrl: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
    val streamUrl: String?,
    val date: LocalDate,
    val startsAt: Instant?,
    val endsAt: Instant?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class EventId(override val value: String): ProjectId {
    companion object { fun random() = EventId(randomUuidString()) }
}

@Serializable
data class EventEdit(
    val eventId: EventId? = null,
    val title: String = "",
    val eventType: EventType = EventType.Show,
    val locationId: LocationId? = null,
    val place: Place? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val contact: String? = null,
    val invitation: String? = null,
    val ageMin: Int? = null,
    val url: String? = null,
    val sourceUrl: String? = null,
    val sourceImageUrl: String? = null,
    val thumbUrl: String? = null,
    val startsAt: Instant? = null,
    val date: LocalDate = (startsAt ?: tomorrowNoon()).toLocalDateTime(TimeZone.currentSystemDefault()).date,
) {
    val isValid get() = title.isNotBlank() && (place != null && place.isValid || locationId != null)
}

fun Event.toEdit() = EventEdit(
    eventId = eventId,
    title = title,
    startsAt = startsAt,
    eventType = eventType,
    locationId = locationId,
    imageUrl = imageUrl,
    description = description,
    url = url,
)

enum class EventType(val label: String) {
    Show("Show"),
    Food("Food"),
    Meet("Meet")
}

enum class EventStatus(override val label: String): LabeledEnum<EventStatus> {
    Pending("Pending"),
    Canceled("Canceled"),
    Live("Live"),
    OnBreak("On Break"),
    Finished("Finished"),
}

@Serializable
data class EventParse(
    val hasContent: Boolean? = null,
    val events: List<EventParseItem>? = null
)

@Serializable
data class EventParseItem(
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
}

private fun toInstant(
    date: LocalDate,
    time: LocalTime,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    return date
        .atTime(time)
        .toInstant(timeZone)
}

@Serializable
data class ReadEventRequest(
    val url: String,
    val isImage: Boolean
)

fun EventParseItem.toEventEdit(
    sourceUrl: String?,
    sourceImageUrl: String?,
): EventEdit? {
    val date = date ?: return null
    return EventEdit(
        title = name ?: "",
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