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
    val contact: String?,
    val invitation: String?,
    val ageMin: Int?,
    val cost: Float?,
    val visibility: Int?,
    val url: String?,
    val sourceUrl: String?,
    val sourceImageUrl: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
    val streamUrl: String?,
    // repeatInterval
    val startsAt: Instant,
    val endsAt: Instant,
    val timeZone: TimeZone,
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
    val title: String? = null,
    val locationId: LocationId? = null,
    val place: Place? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val contact: String? = null,
    val invitation: String? = null,
    val ageMin: Int? = null,
    val cost: Float? = null,
    val isHost: Boolean? = null,
    val url: String? = null,
    val sourceUrl: String? = null,
    val sourceImageUrl: String? = null,
    val thumbUrl: String? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val date: LocalDate? = null,
    val timeZone: TimeZone? = null,
) {
    val isValid get() = invalidPart == null

    val startsAt get() = if (startTime != null && timeZone != null) date?.atTime(startTime)?.toInstant(timeZone) else null
    val endsAt get() = if (endTime != null && timeZone != null) date?.atTime(endTime)?.toInstant(timeZone) else null

    val invalidPart get() = when {
        title.isNullOrBlank() -> "title"
        place != null && place.isValid || locationId != null -> "location"
        startTime == null -> "start time"
        endTime == null -> "end time"
        date == null -> "date"
        timeZone == null -> "timezone"
        else -> null
    }?.let {  }

    val invalidMessage get() = invalidPart?.let { "missing: $it"}
}

fun Event.toEdit() = EventEdit(
    eventId = eventId,
    title = title,
    locationId = locationId,
    imageUrl = imageUrl,
    description = description,
    url = url,
    startTime = startsAt.toLocalDateTime(timeZone).time,
    date = startsAt.toLocalDateTime(timeZone).date,
    timeZone = timeZone,
)

fun EventEdit.mergeLeft(other: EventEdit?) = other?.let {
    EventEdit(
        eventId = eventId ?: it.eventId,
        title = title ?: it.title,
        locationId = locationId ?: it.locationId,
        place = place ?: it.place,
        imageUrl = imageUrl ?: it.imageUrl,
        description = description ?: it.description,
        contact = contact ?: it.contact,
        invitation = invitation ?: it.invitation,
        ageMin = ageMin ?: it.ageMin,
        cost = cost ?: it.cost,
        isHost = isHost ?: it.isHost,
        url = url ?: it.url,
        sourceUrl = sourceUrl ?: it.sourceUrl,
        sourceImageUrl = sourceImageUrl ?: it.sourceImageUrl,
        thumbUrl = thumbUrl ?: it.thumbUrl,
        date = date ?: it.date,
        startTime = startTime ?: it.startTime,
        timeZone = timeZone ?: it.timeZone,
    )
}

fun EventEdit.mergeRight(other: EventEdit?) = other?.mergeLeft(this)

enum class EventStatus(override val label: String): LabeledEnum<EventStatus> {
    Pending("Pending"),
    Canceled("Canceled"),
    Live("Live"),
    OnBreak("On Break"),
    Finished("Finished"),
}