package streetlight.model.data

import kampfire.api.Slug
import koala.model.RouteContent
import kotlinx.serialization.Serializable

/**
 * The settings of a location its host edits: how its events are read from its website, its design, and its
 * subdomain.
 */
@Serializable
data class LocationConfig(
    val locationId: LocationId,
    val parseMode: ParseMode,
    val design: PageDesign?,
    val subdomain: Slug?,
)

/** The content of a location's config page. */
@Serializable
data class LocationConfigContent(
    override val location: Location,
    val config: LocationConfig,
): RouteContent, EventFeedSource {
    override val sourceName get() = location.slug.toString()
    override val url get() = location.eventsUrl
    override val timeZoneId get() = location.timezoneId
    override val parseMode get() = config.parseMode
    override val geoPoint get() = location.geoPoint
}

/** The extent of a feed source's events that is read automatically. */
enum class ParseMode {
    /** Nothing, the page is not read. */
    None,

    /** The events, with their descriptions shortened to a link back to their page. */
    Partial,

    /** The events, with their whole descriptions. */
    Full,
}