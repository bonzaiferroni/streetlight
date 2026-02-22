package streetlight.model.data

import androidx.compose.runtime.Stable
import kampfire.model.LabeledEnum
import kampfire.model.UserId
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
import kotlin.time.Duration.Companion.hours

@Stable
@Serializable
data class Event(
    val eventId: EventId,
    val locationId: LocationId,
    val userId: UserId,
    val currentRequestId: RequestId?,
    val url: String?,
    val sourceUrl: String?,
    val sourceImageUrl: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
    val streamUrl: String?,
    val title: String,
    val description: String?,
    val status: EventStatus,
    val eventType: EventType,
    val contact: String?,
    val invitation: String?,
    val ageMin: Int?,
    val date: LocalDate,
    val startsAt: Instant,
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
    val newLocation: NewLocation? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val url: String? = null,
    val sourceUrl: String? = null,
    val sourceImageUrl: String? = null,
    val thumbUrl: String? = null,
    val contact: String? = null,
    val invitation: String? = null,
    val ageMin: Int? = null,
    val startsAt: Instant = tomorrowNoon(),
    val date: LocalDate = startsAt.toLocalDateTime(TimeZone.currentSystemDefault()).date,
) {
    val isValid get() = title.isNotBlank() && (newLocation != null && newLocation.isValid || locationId != null)
}

fun Event.toUpdate() = EventEdit(
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

//    val title: String,
//    val imageUrl: String? = null,
//    val description: String,
//    val latitude: Double? = null,
//    val longitude: Double? = null,
//    val postedAt: Instant,

private fun toInstant(
    date: LocalDate,
    time: LocalTime,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    return date
        .atTime(time)
        .toInstant(timeZone)
}