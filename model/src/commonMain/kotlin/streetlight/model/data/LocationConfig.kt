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
    val location: Location,
    val config: LocationConfig,
): RouteContent

/** The extent of a feed's events that is read automatically. */
enum class ParseMode {
    /** Nothing, the page is not read. */
    None,

    /** The events, with their descriptions shortened to a link back to their page. */
    Partial,

    /** The events, with their whole descriptions. */
    Full,
}