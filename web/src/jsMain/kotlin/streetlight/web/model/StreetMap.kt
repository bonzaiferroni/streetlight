@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.GeoBounds
import koala.model.GeoMap
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import streetlight.model.data.Community
import streetlight.model.data.EventInfo
import streetlight.model.data.EventType
import streetlight.model.data.Location
import streetlight.model.data.LocationInfo
import kotlin.time.Duration.Companion.minutes

class StreetMap(
    private val scope: CoroutineScope,
    private val client: ClientContext,
    private val geoMap: GeoMap,
) {
    private val state = storeOf(StreetMapState())
    val stateFlow = state.flow
    val stateNow = state.now
    
    val transit = TransitMap(scope, client, geoMap)

    val focusFlow = stateFlow.mapDistinct { it.focus }
    val communityFlow = stateFlow.mapDistinct { it.communities }
    val locationsFlow = stateFlow.mapDistinct { it.locations }
    val spiritFlow = stateFlow.mapDistinct

    init {
        scope.launch {
            geoMap.stateFlow.filter { it.isViewed }.collect { geoMapState ->
                setBounds(geoMapState.bounds, geoMapState.zoom)
            }
        }
    }

    fun toggleLayer(layer: StreetMapLayer) {
        val layers = if (stateNow.layers.contains(layer)) stateNow.layers - layer else stateNow.layers + layer
        if (layer.eventType != null) {
            val events = getBoundedLocations(layers = layers)
            state.set { it.copy(layers = layers, locations = events) }
        } else {
            state.set { it.copy(layers = layers) }
        }
    }

    private val allLocations = ArrayList<LocationInfo>()

    fun setBounds(bounds: GeoBounds, zoom: Float) {
        if (stateNow.isQuerying) return

        if (hasQueried(bounds)) {
            val locations = getBoundedLocations(bounds)
            state.set { it.copy(bounds = bounds, zoom = zoom, locations = locations)}
        } else {
            val queriedBounds = bounds.expandBy(1.2f)
            queries.add(QueryBounds(Clock.System.now(), queriedBounds))
            state.set { it.copy(bounds = bounds, zoom = zoom, queriedBounds = queriedBounds, isQuerying = true)}
            scope.launch {
                val locations = client.api.readLocationsInBounds(queriedBounds) ?: emptyList()
                val mapEntities = locations.mapNotNull { info ->
                    if (allLocations.any { it.locationId == info.locationId }) return@mapNotNull null
                    allLocations.add(info)
                    val events = info.events
                    if (!events.isNullOrEmpty()) {
                        EventEntity(info.location, events)
                    } else {
                        LocationEntity(info.location)
                    }
                }
                geoMap.addEntities(mapEntities)
                val events = getBoundedLocations(bounds)
                state.set { it.copy(locations = events, isQuerying = false)}
            }
        }
    }

    fun spiritVision(isOn: Boolean) {

    }

    private val queries = ArrayList<QueryBounds>()

    private fun hasQueried(bounds: GeoBounds): Boolean {
        val now = Clock.System.now()
        val cutoff = now - 1.minutes
        return queries.any { it.bounds.contains(bounds) && it.time > cutoff }
    }

    private fun getBoundedLocations(
        bounds: GeoBounds = stateNow.bounds,
        layers: Set<StreetMapLayer> = stateNow.layers
    ) = allLocations.filter { location -> bounds.contains(location.geoPoint) }
}

data class StreetMapState(
    val bounds: GeoBounds = GeoBounds.Denver,
    val queriedBounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val locations: List<LocationInfo> = emptyList(),
    val communities: List<Community> = listOf(Community.Eastfax),
    val focus: MapFocus = MapFocus(),
    val layers: Set<StreetMapLayer> = StreetMapLayer.entries.toSet(),
    val isQuerying: Boolean = false,
    val hasSpiritVision: Boolean = false,
) {
    val center get() = bounds.center
}

data class MapFocus(
    val location: Location? = null,
    val event: EventInfo? = null,
)

enum class StreetMapLayer(val label: String, val color: String, val eventType: EventType? = null) {
    Shows("Shows", "#bd7dae", EventType.Show),
    Meet("Meet", "#7dbd8f", EventType.Meet),
    Food("Food", "#bd9a7d", EventType.Food),
    Transit("Transit", "#7daebd"),
    Shelter("Shelter", "#b4bd7d");
}

data class QueryBounds(
    val time: Instant,
    val bounds: GeoBounds,
)