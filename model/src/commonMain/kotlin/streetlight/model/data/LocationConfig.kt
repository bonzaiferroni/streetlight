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

/** How much of a location's events page is read automatically. */
enum class ParseMode {
    None,
    Partial,
    Full,
}