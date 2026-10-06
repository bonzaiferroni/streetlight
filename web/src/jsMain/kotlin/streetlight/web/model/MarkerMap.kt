@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.getContainingBounds
import koala.model.StaticMarker
import koala.model.GeoMap
import kampfire.model.tapOf
import kampfire.model.storeOf
import koala.model.PointMarker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
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
import streetlight.model.data.MediaPost
import streetlight.model.data.Star

/** The entity markers on the map and its focus. */
class MarkerMap(
    private val scope: CoroutineScope,
    val geoMap: GeoMap,
) {
    private val state = storeOf(StreetMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val markerLayer = geoMap.getOrCreateLayer(MarkerLayerConfig.Markers)
    val centerNow get() = geoMap.camera.stateNow.center

    val isMovingState = geoMap.camera.isMovingState
    val focusState = state.tapOf { it.focus }

    init {
        scope.launch {
            geoMap.focusFlow.collect { focus ->
                state.set { copy(focus = focus) }
            }
        }
    }

    /** Replaces the markers with those of [entities]. */
    fun setPoints(entities: List<Entity>) {
        createAndSetMarkers(entities, emptyList())
    }

    /** Adds markers for the [entities] not yet shown. */
    fun addPoints(entities: List<Entity>) {
        createAndSetMarkers(entities, stateNow.markers ?: emptyList())
    }

    /** Keeps the markers that match [predicate]. */
    fun filterPoints(predicate: (StaticMarker) -> Boolean) {
        stateNow.markers?.filter(predicate)?.let {
            setMarkers(it)
        }
    }

    private fun setMarkers(markers: List<StaticMarker>) {
        markerLayer.setPoints(markers)
        state.set { copy(markers = markers, focus = focus.takeIf { f -> markers.any { it.markerId == f?.markerId } }) }
    }

    private fun createAndSetMarkers(entities: List<Entity>, markers: List<StaticMarker> = emptyList()) {
        val markers = markers.toMutableList()
        entities.forEach { entity ->
            if (markers.any { it.markerId == entity.markerId }) return@forEach
            val marker = createMarker(entity) ?: return@forEach
            markers.add(marker)
        }
        setMarkers(markers)
    }

    /** Focuses [marker] and pans to it. */
    fun setFocus(marker: PointMarker) {
        geoMap.setFocus(marker)
        geoMap.camera.panMap(marker.geoPoint)
        state.set { copy(focus = focus)}
    }

    /** Frames every marker. */
    fun showAll() {
        val markers = stateNow.markers.takeIf { !it.isNullOrEmpty() } ?: return
        when(val bounds = getContainingBounds(markers.map { it.geoPoint })) {
            null -> geoMap.camera.panMap(markers.first().geoPoint)
            else -> geoMap.camera.panMap(bounds.scaleBy(1.5f))
        }
    }
}

data class StreetMapState(
    val markers: List<StaticMarker>? = null,
    val focus: PointMarker? = null,
)

/** The markers inside and outside the map view. */
//data class PartitionedMarkers(
//    val bounded: List<EntityMarker>,
//    val unbounded: List<EntityMarker>,
//)

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
}

private fun createMap(posts: List<Entity>): List<StaticMarker> = posts.mapNotNull { createMarker(it) }