package streetlight.web.model

import kampfire.model.reactIn
import koala.model.MarkerLayerConfig
import koala.model.MarkerMap
import koala.model.StaticMarker
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.EventGroup
import streetlight.model.data.City
import streetlight.model.data.CustomEntity
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Entity
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Media
import streetlight.model.data.Offering
import streetlight.model.data.Skill
import streetlight.model.data.MediaPost
import streetlight.model.data.Star

/** Replaces the markers with those of [entities]. */
fun MarkerMap.setPoints(entities: List<Entity>) = setMarkers(entities.toMarkers(emptyList()))

/** Adds markers for the [entities] not yet shown. */
fun MarkerMap.addPoints(entities: List<Entity>) = setMarkers(entities.toMarkers(stateNow.markers))

/** Shows the viewer's location from [starLocator] on its own layer while [scope] is active. */
fun MarkerMap.showStarLocation(starLocator: StarLocator, scope: CoroutineScope) {
    val layer = getOrCreateLayer(MarkerLayerConfig.StarLocation)
    starLocator.starMarkerState.reactIn(scope) { layer.setPoints(listOfNotNull(it)) }
}

/** The [markers], followed by those of these entities that it does not hold. */
private fun List<Entity>.toMarkers(markers: List<StaticMarker>): List<StaticMarker> {
    val ids = markers.mapTo(mutableSetOf()) { it.markerId }
    return markers + mapNotNull { entity -> createMarker(entity)?.takeIf { ids.add(it.markerId) } }
}

private fun createMarker(post: Entity): StaticMarker? = when (post) {
    is EventLocation -> EventMarker(post)
    is EventPost -> EventMarker(post.event)
    is Event -> null
    is Location -> LocationMarker(post)
    is Media -> post.geoPoint?.let { MediaMarker(post, it) }
    is LocationPost -> LocationMarker(post.location)
    is MediaPost -> post.media.geoPoint?.let { MediaMarker(post.media, it) }
    is City -> CityMarker(post)
    is Galaxy -> GalaxyMarker(post)
    is CustomEntity -> null
    is EventGroup -> if (post.startsAt != null) EventInflateMarker(post) else LocationInflateMarker(post)
    is Star -> null
    is Offering -> null
    is Skill -> null
}
