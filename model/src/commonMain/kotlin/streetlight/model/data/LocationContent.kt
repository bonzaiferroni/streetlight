package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class LocationContent(
    val location: Location,
    val events: List<Event>,
    val canEdit: Boolean,
)