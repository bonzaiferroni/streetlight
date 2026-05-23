package streetlight.model.data

import androidx.compose.runtime.Stable
import kampfire.model.Labeled
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Stable
@Serializable
data class Event(
    val eventId: EventId,
    val locationId: LocationId,
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
    val imageRef: Url?,
    val images: ScaledImageArray?,
    val streamUrl: String?,
    val timeZoneId: String,
    val lightCount: Int,
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
value class EventId(override val value: Uuid): ProjectId {
    companion object { fun random() = EventId(Uuid.random()) }
    override fun toString() = value.toString()
}

enum class EventStatus(override val label: String): Labeled {
    Pending("Pending"),
    Canceled("Canceled"),
    Live("Live"),
    OnBreak("On Break"),
    Finished("Finished"),
}