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
    val slug: String,
    val title: String,
    val description: String?,
    val status: EventStatus,
    val contact: String?,
    val invitation: String?,
    val ageMin: Int?,
    val cost: Float,
    val visibility: Int?,
    val links: List<ExtraLink>?,
    val url: String?,
    val sourceUrl: String?,
    val sourceImageUrl: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
    val streamUrl: String?,
    val timeZoneId: String,
    val startsAt: Instant,
    val endsAt: Instant?,
    val updatedAt: Instant,
    val createdAt: Instant,
) {
    val timeZone get() = TimeZone.currentSystemDefault() // notsure
    val isFree get() = cost == 0f

    // repeatInterval
    // val doorsAt: Instant?,
}

@JvmInline
@Serializable
value class EventId(override val value: String): ProjectId {
    companion object { fun random() = EventId(randomUuidString()) }
    override fun toString() = value
}

@Serializable
data class EventEdit(
    val eventId: EventId? = null,
    val title: String? = null,
    val locationId: LocationId? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val contact: String? = null,
    val invitation: String? = null,
    val ageMin: Int? = null,
    val cost: Float? = null,
    val links: List<ExtraLink>? = null,
    val isHost: Boolean? = null,
    val link: String? = null,
    val sourceUrl: String? = null,
    val sourceImageUrl: String? = null,
    val thumbUrl: String? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val date: LocalDate? = null,
    val timeZoneId: String? = null,
) {
    val isFree get() = cost == 0f
    val isValid get() = invalidPart == null
    val timeZone get() = try {
        timeZoneId?.let { TimeZone.of(it) }
    } catch (_: Exception) { TimeZone.currentSystemDefault() } // fails in browser

    val startsAt get() = startTime?.let { timeZone?.let { date?.atTime(startTime)?.toInstant(it) } }
    val endsAt get() = endTime?.let { timeZone?.let { date?.atTime(endTime)?.toInstant(it) }  }

    val invalidPart get() = when {
        title.isNullOrBlank() -> "title"
        locationId == null -> "location"
        startTime == null -> "start time"
        date == null -> "date"
        timeZoneId == null -> "timezone"
        cost == null -> "cost"
        else -> null
    }

    val invalidMessage get() = invalidPart?.let { "missing: $it"}
}

fun Event.toEdit() = EventEdit(
    eventId = eventId,
    title = title,
    locationId = locationId,
    imageUrl = imageUrl,
    description = description,
    contact = contact,
    invitation = invitation,
    ageMin = ageMin,
    cost = cost,
    links = links,
    link = url,
    sourceUrl = sourceUrl,
    sourceImageUrl = sourceImageUrl,
    thumbUrl = thumbUrl,
    startTime = startsAt.toLocalDateTime(timeZone).time,
    date = startsAt.toLocalDateTime(timeZone).date,
    timeZoneId = timeZone.id,
)

fun EventEdit.mergeLeft(other: EventEdit?) = other?.let {
    EventEdit(
        eventId = eventId ?: it.eventId,
        title = title ?: it.title,
        locationId = locationId ?: it.locationId,
        imageUrl = imageUrl ?: it.imageUrl,
        description = description ?: it.description,
        contact = contact ?: it.contact,
        invitation = invitation ?: it.invitation,
        ageMin = ageMin ?: it.ageMin,
        cost = cost ?: it.cost,
        isHost = isHost ?: it.isHost,
        links = links ?: it.links,
        link = link ?: it.link,
        sourceUrl = sourceUrl ?: it.sourceUrl,
        sourceImageUrl = sourceImageUrl ?: it.sourceImageUrl,
        thumbUrl = thumbUrl ?: it.thumbUrl,
        date = date ?: it.date,
        startTime = startTime ?: it.startTime,
        endTime = endTime ?: it.endTime,
        timeZoneId = timeZoneId ?: it.timeZoneId,
    )
} ?: this

fun EventEdit.mergeRight(other: EventEdit?) = other?.mergeLeft(this) ?: this

enum class EventStatus(override val label: String): LabeledEnum<EventStatus> {
    Pending("Pending"),
    Canceled("Canceled"),
    Live("Live"),
    OnBreak("On Break"),
    Finished("Finished"),
}