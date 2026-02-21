package streetlight.model.data

import androidx.compose.runtime.Stable
import kampfire.model.LabeledEnum
import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import streetlight.model.utils.tomorrowNoon
import kotlin.jvm.JvmInline

@Stable
@Serializable
data class Event(
    val eventId: EventId,
    val locationId: LocationId,
    val userId: UserId,
    val currentRequestId: RequestId?,
    val url: String?,
    val imageUrl: String?,
    val streamUrl: String?,
    val title: String,
    val description: String?,
    val status: EventStatus,
    val eventType: EventType,
    val cashTips: Float?,
    val cardTips: Float?,
    val startsAt: Instant,
    val endsAt: Instant,
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
    val startsAt: Instant = tomorrowNoon(),
    val eventType: EventType = EventType.Show,
    val locationId: LocationId? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val url: String? = null,
) {
    val isValid get() = title.isNotBlank() && locationId != null
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
    val time: String? = null,
    val date: String? = null,
    val location: String? = null,
    val address: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
)

//    val title: String,
//    val imageUrl: String? = null,
//    val description: String,
//    val latitude: Double? = null,
//    val longitude: Double? = null,
//    val postedAt: Instant,