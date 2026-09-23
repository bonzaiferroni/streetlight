@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.getContainingBounds
import koala.model.EntityMarker
import koala.model.GeoFocus
import koala.model.GeoMap
import koala.model.MarkerFocus
import kampfire.model.combine
import kampfire.model.tapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
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

class MarkerMap(
    private val scope: CoroutineScope,
    val geoMap: GeoMap,
) {
    private val state = storeOf(StreetMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val markerLayer = geoMap.getOrCreateLayer(MarkerLayerConfig.Markers)
    val centerNow get() = geoMap.camera.stateNow.center

    val markersState = state.tapOf { it.markers }
    val viewMarkersState = markersState.combine(geoMap.camera.movingViewState) { markers, bounds ->
        markers?.partition { bounds.contains(it.geoPoint) }?.let {
            PartitionedMarkers(
                bounded = it.first,
                unbounded = it.second
            )
        }
    }
    val isMovingState = geoMap.camera.isMovingState
    val focusState = state.tapOf { it.focus }

    init {
        scope.launch {
            geoMap.focusFlow.collect { focus ->
                state.set { copy(focus = focus) }
            }
        }
    }

    fun setPoints(entities: List<Entity>) {
        createAndSetMarkers(entities, emptyList())
    }

    fun addPoints(entities: List<Entity>) {
        createAndSetMarkers(entities, stateNow.markers ?: emptyList())
    }

    fun filterPoints(predicate: (EntityMarker) -> Boolean) {
        stateNow.markers?.filter(predicate)?.let {
            setMarkers(it)
        }
    }

    private fun setMarkers(markers: List<EntityMarker>) {
        markerLayer.setPoints(markers)
        state.set { copy(markers = markers, focus = focus.takeIf { f -> markers.any { it.markerId == f?.markerId } }) }
    }

    private fun createAndSetMarkers(entities: List<Entity>, markers: List<EntityMarker> = emptyList()) {
        val markers = markers.toMutableList()
        entities.forEach { entity ->
            if (markers.any { it.markerId == entity.markerId }) return@forEach
            val marker = createMarker(entity) ?: return@forEach
            markers.add(marker)
        }
        setMarkers(markers)
    }

    fun setFocus(marker: EntityMarker) {
        val focus = MarkerFocus(marker)
        geoMap.setFocus(focus)
        geoMap.camera.panMap(marker.geoPoint)
        state.set { copy(focus = focus)}
    }

    fun showAll() {
        val markers = stateNow.markers.takeIf { !it.isNullOrEmpty() } ?: return
        when(val bounds = getContainingBounds(markers.map { it.geoPoint })) {
            null -> geoMap.camera.panMap(markers.first().geoPoint)
            else -> geoMap.camera.panMap(bounds.scaleBy(1.5f))
        }
    }
}

data class StreetMapState(
    val markers: List<EntityMarker>? = null,
    val focus: GeoFocus? = null,
)

data class PartitionedMarkers(
    val bounded: List<EntityMarker>,
    val unbounded: List<EntityMarker>,
)

private fun createMarker(post: Entity): EntityMarker? = when (post) {
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
    is Star -> null
}

private fun createMap(posts: List<Entity>): List<EntityMarker> = posts.mapNotNull { createMarker(it) }