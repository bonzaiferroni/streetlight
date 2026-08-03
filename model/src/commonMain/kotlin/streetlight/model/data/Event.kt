package streetlight.model.data

import androidx.compose.runtime.Stable
import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.Labeled
import kampfire.model.Url
import koala.Image
import kotlinx.serialization.Serializable
import kotlinx.datetime.TimeZone
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Stable
@Serializable
data class Event(
    val eventId: EventId,
    val locationId: LocationId,
    val currentRequestId: RequestId?,
    val slug: Slug,
    val scout: Username?,
    val title: String,
    val description: Markdown?,
    val status: EventStatus,
    val contact: String?,
    val ageMin: Int?,
    val cost: Float?,
    val visibility: Int?,
    val links: List<ExtraLink>?,
    val website: Url?,
    val image: Image?,
    val streamUrl: String?,
    val timeZoneId: String,
    val lightCount: Int,
    val isLit: Boolean,
    val startsAt: Instant?,
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
value class EventId(override val value: Uuid): RecordId {
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