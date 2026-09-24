package streetlight.model.data

import kotlinx.serialization.Serializable

/** The posts and comments of a galaxy, by kind. */
@Serializable
data class PostListing(
    val events: List<EventPost>,
    val locations: List<LocationPost>,
    val comments: List<Comment>,
)

/** The kinds of record a post shares. */
enum class PostType(label: String? = null) {
    Event,
    Location,
    Media;

    val label = label ?: name
}