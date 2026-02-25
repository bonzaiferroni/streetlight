@file:OptIn(FlowPreview::class)

package streetlight.web

import kampfire.model.GeoBounds
import koala.model.BrowserModel
import koala.model.GeoMap
import koala.model.mapDistinct
import koala.model.mapDistinctBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
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
    private val geoMap: GeoMap,
): BrowserModel<StreetMapState>(StreetMapState(), scope) {

    val transit = TransitMap(scope, client, geoMap)

    val focusFlow = stateFlow.mapDistinct { it.focus }
    val communityFlow = stateFlow.mapDistinct { it.communities }
    val eventsFlow = stateFlow.mapDistinct { it.events }
    val eventMapFlow = stateFlow.debounce(100).mapDistinctBy({ it.events }) { it.events.groupBy { event -> event.eventType } }

    init {
        scope.launch {
            geoMap.stateFlow.collect { geoMapState ->
                setBounds(geoMapState.bounds, geoMapState.zoom)
            }
        }
    }

    fun flowOf(eventType: EventType) = eventMapFlow.mapDistinct { it[eventType] ?: emptyList() }

    fun toggleLayer(layer: StreetMapLayer) {
        val layers = if (stateNow.layers.contains(layer)) stateNow.layers - layer else stateNow.layers + layer
        if (layer.eventType != null) {
            val events = getBoundedEvents(layers = layers)
            setState { it.copy(layers = layers, events = events) }
        } else {
            setState { it.copy(layers = layers) }
        }
    }

    private val allEvents = ArrayList<EventInfo>()

    fun setBounds(bounds: GeoBounds, zoom: Float) {
        if (stateNow.isQuerying) return

        if (hasQueried(bounds)) {
            val events = getBoundedEvents(bounds)
            setState { it.copy(bounds = bounds, zoom = zoom, events = events)}
        } else {
            val queriedBounds = bounds.expandBy(1.2f)
            queries.add(QueryBounds(Clock.System.now(), queriedBounds))
            setState { it.copy(bounds = bounds, zoom = zoom, queriedBounds = queriedBounds, isQuerying = true)}
            scope.launch {
                val areaEvents = client.api.queryMap(MapQuery(queriedBounds, stateNow.zoom)) ?: emptyList()
                val mapEntities = areaEvents.mapNotNull { event ->
                    if (allEvents.any { it.eventId == event.eventId }) return@mapNotNull null
                    allEvents.add(event)
                    EventEntity(event)
                }
                geoMap.addEntities(mapEntities)
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

    private fun getBoundedEvents(
        bounds: GeoBounds = stateNow.bounds,
        layers: Set<StreetMapLayer> = stateNow.layers
    ) = allEvents.filter { event -> bounds.contains(event.geoPoint) && layers.any { it.eventType == event.eventType } }
}

data class StreetMapState(
    val bounds: GeoBounds = GeoBounds.Denver,
    val queriedBounds: GeoBounds = GeoBounds.Denver,
    val zoom: Float = 11f,
    val events: List<EventInfo> = emptyList(),
    val communities: List<Community> = listOf(Community.Eastfax),
    val focus: MapFocus = MapFocus(),
    val layers: Set<StreetMapLayer> = StreetMapLayer.entries.toSet(),
    val isQuerying: Boolean = false,
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