package streetlight.model.data

import koala.model.RouteContent
import kotlinx.serialization.Serializable

@Serializable
data class LocationConfig(
    val location: Location,
    val eventSchema: EventSelectorSchema?,
): RouteContent