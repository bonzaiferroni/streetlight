package streetlight.web.model

import koala.model.FeatureMarker
import streetlight.model.data.City
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Media
import streetlight.model.data.MediaPost
import streetlight.model.data.FeedEntity

class MarkerService() {
    // fun createMarkers(posts: List<GalaxyPost>) = posts.mapNotNull { post ->
    //     when (post) {
    //         is EventPost -> EventMarker(post.event)
    //         is LocationPost -> LocationMarker(post.location)
    //         is MediaPost -> post.media.geoPoint?.let { MediaMarker(post.media, it) }
    //     }
    // }

    fun createMarkers(posts: List<FeedEntity>): List<FeatureMarker> = posts.mapNotNull { post ->
        when (post) {
            is EventLocation -> EventMarker(post)
            is EventPost -> EventMarker(post.event)
            is Event -> null
            is Location -> LocationMarker(post)
            is Media -> post.geoPoint?.let { MediaMarker(post, it) }
            is LocationPost -> LocationMarker(post.location)
            is MediaPost -> post.media.geoPoint?.let { MediaMarker(post.media, it) }
            is City -> CityMarker(post)
            is Galaxy -> GalaxyMarker(post)
        }
    }

    // fun createMarkers(galaxies: List<Galaxy>) = galaxies.map { GalaxyMarker(it) }
    // fun createMarkers(cities: List<City>) = cities.map { CityMarker(it) }
}