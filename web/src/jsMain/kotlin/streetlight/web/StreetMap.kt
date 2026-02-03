package streetlight.web

import kampfire.model.GeoBounds
import kampfire.model.meters
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.MapQuery
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import streetlight.model.data.Community
import streetlight.model.data.EventInfo
import streetlight.model.data.EventType
import streetlight.model.data.Location
import kotlin.time.Duration.Companion.minutes

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

    private val allEvents = ArrayList<EventInfo>()

    fun setBounds(bounds: GeoBounds, zoom: Float) {
        if (stateNow.isQuerying || zoom == stateNow.zoom && bounds.center.distanceTo(stateNow.center) < (10 * zoom).meters) return
        // console.log("bounds")

        if (hasQueried(bounds)) {
            val events = getBoundedEvents(bounds)
            setState { it.copy(bounds = bounds, zoom = zoom, events = events)}
        } else {
            val queriedBounds = bounds.expandBy(1.2f)
            setState { it.copy(bounds = bounds, zoom = zoom, queriedBounds = queriedBounds, isQuerying = true)}
            viewModelScope.launch {
                val newEvents = client.event.queryMap(MapQuery(queriedBounds, stateNow.zoom)) ?: emptyList()
                allEvents.addAll(newEvents.filter { newEvent -> allEvents.none { it.eventId == newEvent.eventId } })
                val events = getBoundedEvents(bounds)
                setState { it.copy(events = events, isQuerying = false)}
            }
        }
    }

    private val queries = ArrayList<QueryBounds>()

    private fun hasQueried(bounds: GeoBounds): Boolean {
        val now = Clock.System.now()
        val cutoff = now - 1.minutes
        return queries.any { it.bounds.contains(bounds) && it.time > cutoff }
    }

    private fun getBoundedEvents(bounds: GeoBounds) = allEvents.filter { bounds.contains(it.geoPoint) }
}

data class StreetMapState(
    val bounds: GeoBounds = GeoBounds.Denver,
    val queriedBounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val events: List<EventInfo> = emptyList(),
    val communities: List<Community> = listOf(Community.BFEastfax),
    val name: String = "",
    val focus: MapFocus = MapFocus(),
    val layers: Set<MapLayer> = MapLayer.entries.toSet(),
    val isQuerying: Boolean = false,
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

data class QueryBounds(
    val time: Instant,
    val bounds: GeoBounds,
)