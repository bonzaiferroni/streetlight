package streetlight.web.ui

import koala.dom.UIMessage
import koala.dom.set
import koala.model.GeoMap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.ParseRequest
import streetlight.web.io.ApiClient

class ScoutMap(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val geoMap: GeoMap,
) {
    private val state = storeOf(ScoutMapState())
    private val editState = storeOf<LocationEdit?>(null)
    private val msg = storeOf(UIMessage(initialMsg))
    val editFlow = editState.flow
    val stateFlow = state.flow
    val messageFlow = msg.flow

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

    suspend fun postLocation(): Location? {
        val edit = editState.now?.takeIf { it.isValid } ?: return null
        msg.set("Posting...")
        return api.createOrEditLocation(edit)
    }
}

data class ScoutMapState(
    val link: String = "",
)

private const val initialMsg = "Enter a link for the location you'd like to put on the map, or edit the details yourself."