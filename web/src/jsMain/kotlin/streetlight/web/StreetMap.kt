package streetlight.web

import kampfire.model.GeoBounds
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.MapQuery
import kotlinx.coroutines.launch
import streetlight.model.data.Community
import streetlight.model.data.EventInfo
import streetlight.model.data.EventType
import streetlight.model.data.Location

class StreetMap(
    scope: CoroutineScope,
    private val client: ClientContext,
): BrowserModel<StreetMapState>(StreetMapState(), scope) {

    val focusFlow = stateFlow.mapDistinct { it.focus }
    val communityFlow = stateFlow.mapDistinct { it.communities }
    val eventsFlow = stateFlow.mapDistinct { it.events }
    val eventMapFlow = stateFlow.mapDistinctBy({ it.events }) { it.events.groupBy { event -> event.eventType } }

    fun flowOf(eventType: EventType) = eventMapFlow.mapDistinct { it[eventType] ?: emptyList() }

    fun setName(name: String) {
        setState { it.copy(name = name) }
    }

    fun toggleLayer(layer: MapLayer) {
        val layers = if (stateNow.layers.contains(layer)) stateNow.layers - layer else stateNow.layers + layer
        setState { it.copy(layers = layers) }
    }

    fun setBounds(bounds: GeoBounds, zoom: Float) {
        if (stateNow.queriedBounds.contains(bounds)) {
            setState { it.copy(bounds = bounds, zoom = zoom)}
        } else {
            val queriedBounds = bounds.expandBy(1.5f)
            setState { it.copy(bounds = bounds, zoom = zoom, queriedBounds = queriedBounds )}
            viewModelScope.launch {
                val events = client.event.queryMap(MapQuery(queriedBounds, stateNow.zoom)) ?: emptyList()
                setState { it.copy(events = events)}
            }
        }
    }
}

data class StreetMapState(
    val bounds: GeoBounds = GeoBounds.Denver,
    val queriedBounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val events: List<EventInfo> = emptyList(),
    val communities: List<Community> = listOf(Community.BFEastfax),
    val name: String = "",
    val focus: MapFocus = MapFocus(),
    val layers: Set<MapLayer> = MapLayer.entries.toSet()
) {
    val center get() = bounds.center
}

data class MapFocus(
    val location: Location? = null,
    val event: EventInfo? = null,
)

enum class MapLayer(val label: String, val eventType: EventType? = null) {
    Shows("Shows", EventType.Show),
    Food("Food", EventType.Food),
    Fellowship("Fellowship", EventType.Fellowship),
    Transit("Transit"),
    Shelter("Shelter"),
}