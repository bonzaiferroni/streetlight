package streetlight.web.ui

import kampfire.model.GeoPoint
import koala.dom.UIMessage
import koala.dom.set
import koala.model.GeoMap
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.ParseRequest
import streetlight.web.io.ApiClient
import streetlight.web.model.AppContext

class LocationScout(
    private val scope: CoroutineScope,
    override val app: AppContext,
): ViewModel {
    private val state = storeOf(LocationScoutState())
    private val msg = storeOf(UIMessage(introMsg))
    private val data = storeOf(LocationScoutData())
    val dataFlow = data.flow
    val stateFlow = state.flow
    val messageFlow = msg.flow

    private var count = 0

    fun setLink(value: String) {
        state.set { it.copy(link = value) }
    }

    fun readLink() {
        val link = state.now.link.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            msg.set("Reading the link, this will take a minute.")
            val edit = api.parseLocation(ParseRequest(link)) ?: return@launch
            msg.set("Does this information look correct?")
            data.set { it.copy(edit = edit.copy(geoPoint = geoMap.stateNow.center)) }
        }
    }

    fun reset() {
        state.set { LocationScoutState() }
        data.set { LocationScoutData() }
        msg.set("$introMsg\n\nLocations added: $count")
    }

    fun searchOSM() {

    }

    fun here() {
        data.set { it.copy(point = geoMap.stateNow.center) }
        msg.set(detailsMsg)
    }

    fun postLocation() {
        val edit = data.now.edit?.takeIf { it.isValid } ?: return
        msg.set("Posting ${edit.name}...")
        scope.launch {
            val location = api.createOrEditLocation(edit)
            if (location == null) {
                msg.set("Something went wrong")
                return@launch
            }
            count++
            data.set { it.copy(location = location) }
            msg.set("Posted. You can now add events to ${location.name} or add another location.")
        }
    }
}

data class LocationScoutState(
    val link: String = "",
)

data class LocationScoutData(
    val point: GeoPoint? = null,
    val location: Location? = null,
    val edit: LocationEdit? = null,
)

private val introMsg = """
    Earth, it is full of locations. Let's add one to the map. Find the point where it will go, or search OpenStreetMap
    by the location's name, address, or some other identifier. 
    
    We can also ask OpenStreetMap for a list of locations in the area.
""".trimIndent()

private val detailsMsg = """
    Does this location have a link? We can try to read the details from that link or you can enter them yourself.
""".trimIndent()

