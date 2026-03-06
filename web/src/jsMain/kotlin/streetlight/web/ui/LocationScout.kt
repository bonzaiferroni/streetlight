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
import streetlight.model.data.merge
import streetlight.model.data.toEdit
import streetlight.model.external.toPlace
import streetlight.web.io.SvgPath
import streetlight.web.model.AppContext
import streetlight.web.model.IconEntity

class LocationScout(
    private val scope: CoroutineScope,
    override val app: AppContext,
): ViewModel {
    private val state = storeOf(LocationScoutState())
    private val msg = storeOf(UIMessage(introMsg))
    val stateFlow = state.flow
    val stateNow get() = state.now
    val messageFlow = msg.flow

    private var count = 0

    fun setLink(value: String) {
        state.set { it.copy(website = value) }
    }

    fun readLink() {
        val website = state.now.website.takeIf { it.startsWith("http") } ?: return
        val currentEdit = state.now.edit ?: return
        scope.launch {
            msg.set("Reading the link, this will take a minute.")
            val edit = api.parseLocation(ParseRequest(website))?.merge(currentEdit) ?: return@launch
            msg.set("Does this information look correct?")
            state.set { it.copy(edit = edit) }
        }
    }

    fun reset() {
        state.set { LocationScoutState() }
        msg.set("$introMsg Locations added so far: $count")
    }

    fun setQuery(value: String) {
        state.set { it.copy(query = value) }
    }

    fun setLimitMap(value: Boolean) {
        state.set { it.copy(limitMap = value) }
    }

    fun setLimitCity(value: Boolean) {
        state.set { it.copy(limitCity = value) }
    }

    fun setLimitState(value: Boolean) {
        state.set { it.copy(limitState = value) }
    }

    fun searchOSM() {
//        val q = state.now.query.takeIf { it.isNotBlank() }?.let {
//            OSMQuery(
//                amenity = it,
//                city = stateNow.city.takeIf { stateNow.limitCity },
//                state = stateNow.state.takeIf { stateNow.limitState },
//                bounds = geo.stateNow.bounds.takeIf { stateNow.limitMap },
//            )
//        } ?: return
        val query = state.now.query.takeIf { it.isNotBlank() }?.let {
            val city = stateNow.city
            if (city != null && !it.contains(city) && stateNow.limitCity) {
                "$it, $city"
            } else it
        }?.let {
            val state = stateNow.state
            if (state != null && !it.contains(state) && stateNow.limitState) {
                "$it, $state"
            } else it
        } ?: return
        val bounds = geo.stateNow.bounds.takeIf { stateNow.limitMap }
        msg.set("Searching OpenStreetMap...")
        scope.launch {
            val places = osm.readPlaces(query, bounds)?.mapNotNull { it.toPlace().takeIf { p -> p.geoPoint != null } }
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

    fun setEdit(edit: LocationEdit) {
        state.set { it.copy(edit = edit) }
    }

    private fun addConstructionMarker(point: GeoPoint) {
        geo.tempEntities(listOf(
            IconEntity("here", SvgPath.guitar, point)
        ))
    }

    fun choosePlace(place: Place) {
        val website = place.website
        val point = place.geoPoint ?: error("geoPoint is null")
        addConstructionMarker(point)
        val edit = place.toEdit()
        state.set { it.copy(place = place, website = website ?: "", edit = edit, point = point)}
        if (website != null) {
            msg.set("OSM provided a website for the location, we can try to read it.")
        } else {
            msg.set("If there is a website for this location we can try to read it.")
        }
    }

    fun postLocation() {
        val edit = state.now.edit?.takeIf { it.isValid } ?: return
        msg.set("Posting ${edit.name}...")
        scope.launch {
            val location = api.createOrEditLocation(edit)
            if (location == null) {
                msg.set("Something went wrong")
                return@launch
            }
            count++
            state.set { it.copy(location = location) }
            msg.set("Posted. You can now add events to ${location.name} or add another location.")
            geo.tempEntities(null)
        }
    }
}

data class LocationScoutState(
    val website: String = "",
    val query: String = "",
    val point: GeoPoint? = null,
    val place: Place? = null,
    val city: String? = "Aurora",
    val state: String? = "CO",
    val edit: LocationEdit? = null,
    val limitMap: Boolean = false,
    val limitCity: Boolean = false,
    val limitState: Boolean = true,
    val location: Location? = null,
    val places: List<Place>? = null,
)

private val introMsg = """
    Earth, it is full of locations. Let's add one to the map. But where?
""".trimIndent()

private val detailsMsg = """
    Does this location have a link? We can try to read the details from that link or you can enter them yourself.
""".trimIndent()

