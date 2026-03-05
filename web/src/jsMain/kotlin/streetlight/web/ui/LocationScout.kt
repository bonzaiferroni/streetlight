package streetlight.web.ui

import kampfire.model.GeoPoint
import koala.dom.UIMessage
import koala.dom.set
import koala.model.GeoMap
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.ParseRequest
import streetlight.web.io.ApiClient

class LocationScout(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val geoMap: GeoMap,
) {
    private val state = storeOf(ScoutMapState())
    private val editState = storeOf<LocationEdit?>(null)
    private val msg = storeOf(UIMessage(initialMsg))
    private val locationState = storeOf<Location?>(null)
    val editFlow = editState.flow
    val stateFlow = state.flow
    val messageFlow = msg.flow
    val locationFlow = locationState.flow
    val pointFlow = stateFlow.mapDistinct { it.point }

    fun setLink(value: String) {
        state.set { it.copy(link = value) }
    }

    fun readLink() {
        val link = state.now.link.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            msg.set("Reading the link, this will take a minute.")
            val edit = api.parseLocation(ParseRequest(link)) ?: return@launch
            msg.set("Does this information look correct?")
            editState.set { edit.copy(geoPoint = geoMap.stateNow.center) }
        }
    }

    fun reset() {
        state.set { ScoutMapState() }
        editState.set { null }
        msg.set(initialMsg)
        locationState.set { null }
    }

    fun postLocation() {
        val edit = editState.now?.takeIf { it.isValid } ?: return
        msg.set("Posting ${edit.name}...")
        scope.launch {
            val location = api.createOrEditLocation(edit)
            if (location == null) {
                msg.set("Something went wrong")
                return@launch
            }
            locationState.set { location }
            msg.set("Posted. You can now add events to ${location.name} or add another location.")
        }
    }
}

data class ScoutMapState(
    val link: String = "",
    val point: GeoPoint? = null,
)

private const val initialMsg = "Enter a link for the location you'd like to put on the map, or edit the details yourself."