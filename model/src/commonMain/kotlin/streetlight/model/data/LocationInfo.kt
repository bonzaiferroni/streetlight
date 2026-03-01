package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class LocationInfo(
    val location: Location,
    val events: List<Event>
)