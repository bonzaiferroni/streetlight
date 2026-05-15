@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.distanceTo
import kampfire.model.handleResponse
import kampfire.model.kilometers
import koala.dom.UIMessage
import koala.dom.set
import koala.model.GeoMap
import koala.model.PanPoint
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.mergeRight
import streetlight.model.data.toEdit
import streetlight.model.external.toPlace
import streetlight.web.io.ApiClient
import streetlight.web.io.OSMClient
import streetlight.web.ui.ViewModel

class LocationFinder(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val osm: OSMClient,
    private val geo: GeoMap
) {

    private val state = storeOf(LocationFinderState())
    val stateFlow = state.flow

    val msg = storeOf<UIMessage?>(null)

    val editor = LocationEditor(null, scope, api)

    val queryFlow = stateFlow.mapDistinct { it.query }

    init {
        scope.launch {
            stateFlow.mapDistinct { it.query }.debounce(500).collect { query ->
                val locations = if (query.isBlank()) {
                    emptyList()
                } else {
                    api.searchLocations(query).handleResponse(msg::set) ?: emptyList()
                }
                state.set { it.copy(locations = locations) }
            }
        }
    }

    fun setQuery(value: String) {
        state.set { it.copy(query = value) }
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

            val edit = place.toEdit()
            editor.setEdit { edit.mergeRight(it) }
            msg.set("Is this what you are looking for?")
        }
    }
}

data class LocationFinderState(
    val query: String = "",
    val locations: List<Location> = emptyList(),
)