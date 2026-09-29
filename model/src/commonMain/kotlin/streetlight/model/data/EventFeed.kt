package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A page listing events whose locations are read along with them. */
sealed interface EventFeed : Lead {
    val name: String
    val timeZoneId: String
    val parseMode: ParseMode
    val geoPoint: GeoPoint
    val location: Location?

    override val leadType get() = LeadType.EventFeed
}

/** The events page of a [location]. */
@Serializable
data class LocationEventFeed(
    override val location: Location,
    override val initialUrl: Url,
    override val parseMode: ParseMode,
): EventFeed {
    override val name get() = location.slug.toString()
    override val timeZoneId get() = location.timezoneId
    override val geoPoint get() = location.geoPoint
}

/**
 * A page listing local events at many locations, such as a newspaper's calendar, checked again as an
 * [LeadType.EventFeed] or read once as an [LeadType.EventScan].
 */
@Serializable
data class GeneralEventFeed(
    val leadId: LeadId,
    override val name: String,
    override val initialUrl: Url,
    override val geoPoint: GeoPoint,
    override val timeZoneId: String,
    val checkedAt: Instant?,
    val createdAt: Instant,
    override val leadType: LeadType = LeadType.EventFeed,
): EventFeed {
    override val parseMode get() = ParseMode.Partial
    override val location: Location? get() = null
}
