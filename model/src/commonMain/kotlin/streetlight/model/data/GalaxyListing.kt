package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class GalaxyListing(
    val events: List<EventPost>?,
    val locations: List<LocationPost>?,
) {
    val types get() = buildSet {
        if (events != null) add(PostType.Event)
        if (locations != null) add(PostType.Location)
    }
}

enum class PostType(label: String? = null) {
    Event,
    Location;

    val label = label ?: name
}