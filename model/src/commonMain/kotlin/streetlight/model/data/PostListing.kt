package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class PostListing(
    val events: List<EventPost>,
    val locations: List<LocationPost>,
    val comments: List<Comment>,
)

enum class PostType(label: String? = null) {
    Event,
    Location,
    Media;

    val label = label ?: name
}