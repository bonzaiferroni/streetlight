package streetlight.model.data

import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class LocationConfig(
    val locationId: LocationId,
    val parseMode: ParseMode,
    val layout: Layout?,
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