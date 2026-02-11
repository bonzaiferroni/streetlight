package streetlight.model.data

import androidx.compose.runtime.Stable
import kampfire.model.LabeledEnum
import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
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
    val title: String = "",
    val startsAt: Instant = tomorrowNoon(),
    val eventType: EventType = EventType.Show,
    val locationId: LocationId? = null,
    val imageUrl: String? = null,
    val description: String? = null,
) {
    val isValid get() = title.isNotBlank() && locationId != null
//    fun toEvent() = Event(
//        eventId = EventId.random(),
//        locationId = locationId,
//        userId = UserId.random(),
//        currentRequestId = null,
//        url = null,
//        imageUrl = null,
//        streamUrl = null,
//        title = title,
//        description = null,
//        status = EventStatus.Pending,
//        eventType = eventType,
//        cashTips = null,
//        cardTips = null,
//        startsAt = startsAt,
//        endsAt = startsAt,
//        updatedAt = Instant.DISTANT_PAST,
//        createdAt = Instant.DISTANT_PAST,
//    )
}

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
