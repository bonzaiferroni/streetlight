package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A page listing events whose locations are read along with them. */
sealed interface EventFeed {
    /** The name of the feed, for its reports. */
    val name: String
    val url: Url
    val timeZoneId: String
    val parseMode: ParseMode
    val geoPoint: GeoPoint

    /** The location its events take place at unless they name another. */
    val location: Location?
}

/** The events page of a [location]. */
@Serializable
data class LocationEventFeed(
    override val location: Location,
    override val url: Url,
    override val parseMode: ParseMode,
): EventFeed {
    override val name get() = location.slug.toString()
    override val timeZoneId get() = location.timezoneId
    override val geoPoint get() = location.geoPoint
}

/** A page listing local events at many locations, such as a newspaper's calendar. */
@Serializable
data class GeneralEventFeed(
    val eventFeedId: EventFeedId,
    override val name: String,
    override val url: Url,
    override val geoPoint: GeoPoint,
    override val timeZoneId: String,
    val checkedAt: Instant?,
    val createdAt: Instant,
): EventFeed {
    override val parseMode get() = ParseMode.Partial
    override val location: Location? get() = null
}

@JvmInline
@Serializable
value class EventFeedId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}
