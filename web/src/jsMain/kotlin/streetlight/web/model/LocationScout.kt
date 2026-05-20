package streetlight.web.model

import koala.dom.UIMessageType
import koala.model.GeoMap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.mergeLeft
import streetlight.model.data.toEdit
import streetlight.model.external.OSMLocation
import streetlight.model.external.OSMQuery
import streetlight.web.io.ApiClient
import streetlight.web.io.OSMClient

class LocationScout(
    val galaxy: Galaxy,
    private val editor: LocationEditor,
    private val scope: CoroutineScope,
    private val osm: OSMClient,
    private val geo: GeoMap,
    private val toaster: Toaster,
    private val api: ApiClient,
) {
    private val state = storeOf(LocationScoutState(city = galaxy.city))
    val stateNow get() = state.now
    val stateFlow = state.flow
    // val editor = LocationEditor(LocationEdit(), scope, app)
    // val finder = LocationFinderProto(scope, app)

    init {
        scope.launch {
            // api.searchLocations()
        }
    }

    fun queryOSM() {
        val query = stateNow.query
        if (query.isBlank()) return
        scope.launch {
            val query = OSMQuery(query = query, state = stateNow.city)
            val location = osm.readPlaces(query)?.firstOrNull() ?: return@launch
            setEdit(location)
        }
    }

    fun reverseQueryOSM() {
        val center = geo.stateNow.center
        scope.launch {
            val location = osm.readPlaceAt(center)
            if (location == null) {
                toaster.toast("Unable to read place", UIMessageType.Error)
                return@launch
            }
            setEdit(location)
        }
    }

    private fun setEdit(location: OSMLocation) {
        editor.setEdit { location.toEdit().mergeLeft(it) }
    }
}

data class LocationScoutState(
    val query: String = "",
    val city: String? = null,
    val locations: List<Location> = emptyList()
)