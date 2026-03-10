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
    val cost: Float?,
    val visibility: Int?,
    val url: String?,
    val sourceUrl: String?,
    val sourceImageUrl: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
    val streamUrl: String?,
    val date: LocalDate,
    // repeatInterval
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
    val title: String? = null,
    val eventType: EventType? = null,
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
    val startsAt: Instant? = null,
    val date: LocalDate? = null,
) {
    val isValid get() = !title.isNullOrBlank()
            && ((place != null && place.isValid) || locationId != null)
            && date != null
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

fun EventEdit.mergeLeft(other: EventEdit?) = other?.let {
    EventEdit(
        eventId = eventId ?: it.eventId,
        title = title ?: it.title,
        eventType = eventType ?: it.eventType,
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
        startsAt = startsAt ?: it.startsAt,
        date = date ?: it.date,
    )
}

fun EventEdit.mergeRight(other: EventEdit?) = other?.mergeLeft(this)

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