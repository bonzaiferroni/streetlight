package streetlight.model.data

import kampfire.api.toMarkdown
import kabinet.utils.toAgoFormat
import kotlin.time.Clock
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

/** An event at a location. */
@Stable
@Serializable
data class Event(
    val eventId: EventId,
    val locationId: LocationId,
    val currentRequestId: RequestId?,
    val slug: Slug,
    val host: Username?,
    val title: String,
    override val description: Markdown?,
    val tags: List<EventTag>?,
    val status: EventStatus,
    val contact: String?,
    val ageMin: Int?,
    val cost: Float?,
    val visibility: Int?,
    override val links: List<ExtraLink>?,
    override val url: Url?,
    override val image: Image?,
    val streamUrl: String?,
    val timeZoneId: String,
    val lightCount: Int,
    val isLit: Boolean,
    val startsAt: Instant?,
    val endsAt: Instant?,
    val updatedAt: Instant,
    override val createdAt: Instant,
): Entity {
    val timeZone get() = TimeZone.currentSystemDefault() // notsure
    val isFree get() = cost == 0f

    /** The first of [tags], or `null` when it has none. */
    val tag get() = tags?.firstOrNull()

    override val label get() = title
    override val body get() = description ?: startsAt?.let { startsAtBodyOf(it) } ?: addedBodyOf(createdAt)
    override val geoPoint get() = null
    override val markerId get() = eventId.toString()

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

/** A body that tells when an event starts, or started, at [startsAt]. */
private fun startsAtBodyOf(startsAt: Instant) = when (startsAt > Clock.System.now()) {
    true -> "Starts ${startsAt.toAgoFormat()}"
    false -> "Started ${startsAt.toAgoFormat()}"
}.toMarkdown()
