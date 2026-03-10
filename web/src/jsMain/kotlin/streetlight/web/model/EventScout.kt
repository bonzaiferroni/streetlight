@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.distanceTo
import kampfire.model.kilometers
import koala.dom.UIMessage
import koala.dom.set
import koala.model.PanPoint
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.EventEdit
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Place
import streetlight.model.data.mergeRight
import streetlight.model.data.toEdit
import streetlight.model.external.toPlace
import streetlight.web.io.SvgPath
import streetlight.web.ui.ViewModel

class EventScout(
    override val app: Streetlight,
    val scope: CoroutineScope,
): ViewModel {
    private val state = storeOf(EventScoutState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    private val msg = storeOf(UIMessage(introMsg))
    val messageFlow = msg.flow

    init {
        scope.launch {
            stateFlow.mapDistinct { it.query }.debounce(500).collect { query ->
                val locations = if (query.isBlank()) {
                    emptyList()
                } else {
                    api.searchLocations(query) ?: emptyList()
                }
                state.set { it.copy(locations = locations) }
            }
        }
    }

    fun setQuery(value: String) {
        state.set { it.copy(query = value) }
    }

    fun here() {
        msg.set("Looking for information about that place on OpenStreetMap...")
        val point = geo.stateNow.center
        scope.launch {
            val place = osm.readPlace(point)?.toPlace()?.copy(geoPoint = point)
            if (place == null) {
                // td: handle
                msg.set("Something went wrong")
                return@launch
            }
            choosePlace(place)
        }
    }

    fun choosePlace(place: Place) {
        val website = place.website
        val point = place.geoPoint ?: error("geoPoint is null")
        addConstructionMarker(point)
        val edit = place.toEdit().mergeRight(state.now.locationEdit)
        state.set { it.copy(website = website ?: "", locationEdit = edit, point = point)}
        if (website != null) {
            msg.set("OSM provided a website for the location, we can try to read it.")
        } else {
            msg.set("If there is a website for this location we can try to read it.")
        }
    }

    fun searchQuery() {
        val query = state.now.query.takeIf { it.isNotBlank() } ?: return
        msg.set("Searching OSM: ${state.now.query}")
        scope.launch {
            val places = osm.readPlaces(query)?.map { it.toPlace() }
                ?.filter{ p -> p.geoPoint?.let { gp -> gp.distanceTo(geo.stateNow.center) < 100.kilometers } ?: false }
            val place = places?.firstOrNull()
            if (place == null) {
                msg.set("We couldn't find anything.")
                return@launch
            }
            place.geoPoint?.let {
                geo.panMap(PanPoint(point = it, zoom = 15f))
            }

            val edit = place.toEdit().mergeRight(state.now.locationEdit)
            state.set { it.copy(locationEdit = edit) }
            msg.set("Is this what you are looking for?")
        }
    }

    private fun addConstructionMarker(point: GeoPoint) {
        geo.tempEntities(listOf(
            IconEntity("here", SvgPath.guitar, point)
        ))
    }

    fun reset() {
        state.set { EventScoutState() }
    }
}

data class EventScoutState(
    val query: String = "",
    val website: String = "",
    val locations: List<Location> = emptyList(),
    val point: GeoPoint? = null,
    val locationEdit: LocationEdit? = null,
    val location: Location? = null,
    val event: EventEdit? = null,
)

private const val introMsg = "Where will the event be held? Search for a location or find one on the map."

