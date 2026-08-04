package streetlight.model.data

import koala.model.RouteContent
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class LocationConfig(
    val locationId: LocationId,
)

@Serializable
data class LocationConfigContent(
    val location: Location,
    val config: LocationConfig,
    val origins: List<Origin>
): RouteContent

