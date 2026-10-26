package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable

/** The crawler's reading of a location's events: from [eventsUrl], to the extent of [parseMode]. */
@Serializable
data class ParseConfig(
    val locationId: LocationId,
    val eventsUrl: Url?,
    val parseMode: ParseMode,
)
