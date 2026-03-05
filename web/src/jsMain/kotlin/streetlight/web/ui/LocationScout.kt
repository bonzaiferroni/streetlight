package streetlight.web.ui

import kampfire.model.GeoPoint
import koala.dom.UIMessage
import koala.dom.set
import koala.model.PanPoint
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.ParseRequest
import streetlight.model.data.Place
import streetlight.model.external.toPlace
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
        state.set { it.copy(website = value) }
    }

    fun readLink() {
        val link = state.now.website.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            msg.set("Reading the link, this will take a minute.")
            val parsedEdit = api.parseLocation(ParseRequest(link)) ?: return@launch
            msg.set("Does this information look correct?")
            val edit = parsedEdit.copy(
                geoPoint = geo.stateNow.center,
                address = state.now.place?.address ?: parsedEdit.address
            )
            data.set { it.copy(edit = edit) }
        }
    }

    fun reset() {
        state.set { LocationScoutState() }
        data.set { LocationScoutData() }
        msg.set("$introMsg. Locations added so far: $count")
    }

    fun setQuery(value: String) {
        state.set { it.copy(query = value) }
    }

    fun searchOSM() {
        val query = state.now.query.takeIf { it.isNotBlank() } ?: return
        msg.set("Searching OpenStreetMap...")
        scope.launch {
            val places = osm.readPlaces(query)?.map { it.toPlace() }
            if (places.isNullOrEmpty()) {
                msg.set("We couldn't find anything.")
                return@launch
            }
            places.firstOrNull()?.geoPoint?.let {
                geo.panMap(PanPoint(point = it, zoom = 15f))
            }

            state.set { it.copy(places = places) }
            msg.set("Is this what you are looking for?")
        }
    }

    fun here() {
        data.set { it.copy(point = geo.stateNow.center) }
        msg.set(detailsMsg)
    }

    fun choosePlace(place: Place) {
        val website = place.website
        state.set { it.copy(place = place, website = website ?: "")}
        data.set { it.copy(point = place.geoPoint) }
        if (website != null) {
            msg.set("OSM provided a website for the location, we can try to read it.")
        } else {
            msg.set("If there is a website for this location we can try to read it.")
        }
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
    val website: String = "",
    val query: String = "",
    val places: List<Place> = emptyList(),
    val place: Place? = null,
)

data class LocationScoutData(
    val point: GeoPoint? = null,
    val location: Location? = null,
    val edit: LocationEdit? = null,
)

private val introMsg = """
    Earth, it is full of locations. Let's add one to the map, where should it go?
""".trimIndent()

private val detailsMsg = """
    Does this location have a link? We can try to read the details from that link or you can enter them yourself.
""".trimIndent()

