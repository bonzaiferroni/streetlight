package streetlight.model.data

import androidx.compose.runtime.Stable
import kampfire.model.LabeledEnum
import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
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
data class NewEvent(
    val locationId: LocationId,
    val title: String,
    val startsAt: Instant,
    val eventType: EventType,
)

enum class EventType(val label: String) {
    Show("Show"),
    Food("Food"),
    Fellowship("Fellowship")
}

enum class EventStatus(override val label: String): LabeledEnum<EventStatus> {
    Pending("Pending"),
    Canceled("Canceled"),
    Live("Live"),
    OnBreak("On Break"),
    Finished("Finished"),
}