package streetlight.web.model

import streetlight.model.data.EventPost
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.model.data.LocationPost
import streetlight.model.data.MediaPost

class MarkerService() {
    fun createMarkers(posts: List<GalaxyPost>) = posts.mapNotNull { post ->
        when (post) {
            is EventPost -> EventMarker(post)
            is LocationPost -> return@mapNotNull null
            is MediaPost -> return@mapNotNull null
        }
    }

//    fun <T: Any> createMarkersProto(values: List<T>) = values.mapNotNull { createMarker(it) }
//        .groupBy { it.markerId }
//        .map { if (it.value.size == 1) it.value else  }

    fun createMarkers(galaxies: List<Galaxy>) = galaxies.map {
        GalaxyMarker(it)
    }

    fun createMarker(value: Any) = when (value) {
        is EventPost -> EventMarker(value)
        is LocationPost -> null
        is MediaPost -> null
        is Galaxy -> GalaxyMarker(value)
        else -> null
    }
}