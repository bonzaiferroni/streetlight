package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class HomeContent(
    val galaxies: List<Galaxy>,
    val posts: List<Post>
)