package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class GalaxyContent(
    val galaxy: Galaxy,
    val posts: List<GalaxyPost>,
)