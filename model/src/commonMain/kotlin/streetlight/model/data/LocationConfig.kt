package streetlight.model.data

import kampfire.api.Slug
import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class LocationConfig(
    val locationId: LocationId,
    val parseMode: ParseMode,
    val design: PageDesign?,
    val subdomain: Slug?,
)

@Serializable
data class LocationConfigContent(
    val location: Location,
    val config: LocationConfig,
): RouteContent

enum class ParseMode {
    None,
    Partial,
    Full,
}