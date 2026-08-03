package streetlight.model.data

import koala.model.RouteContent
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class LocationConfig(
    val locationId: LocationId,
    val eventSchema: EventSelectorSchema?,
)

@Serializable
data class LocationConfigContent(
    val location: Location,
    val config: LocationConfig,
    val parseResult: EventParseResult?,
    val parsedAt: Instant?,
): RouteContent

@Serializable
data class EventParseResult(
    val isSuccess: Boolean,
    val eventCount: Int,
    val newCount: Int,
)