package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class LocationContent(
    val location: Location,
    val events: List<Event>,
    val canEdit: Boolean,
)

@Serializable
data class LocationUpdaterContent(
    val location: Location,
    val editLogs: List<EditLog>
)

@Serializable
data class EventUpdaterContent(
    val event: Event,
    val editLogs: List<EditLog>
)

@Serializable
data class GalaxyContent(
    val galaxy: Galaxy,
    val posts: List<GalaxyPost>,
)

@Serializable
data class StarContent(
    val star: Star,
    val posts: List<Media>,
    val isCaller: Boolean,
)