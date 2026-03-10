package streetlight.web.ui

import kampfire.model.GeoPoint
import kampfire.model.distanceTo
import kampfire.model.kilometers
import koala.dom.UIMessage
import koala.dom.set
import koala.model.PanPoint
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Place
import streetlight.model.data.UrlParseRequest
import streetlight.model.data.mergeLeft
import streetlight.model.data.mergeRight
import streetlight.model.data.toEdit
import streetlight.model.external.toPlace
import streetlight.web.io.SvgPath
import streetlight.web.model.Streetlight
import streetlight.web.model.IconEntity

class LocationScout(
    private val scope: CoroutineScope,
    override val app: Streetlight,
): ViewModel {
    private val state = storeOf(LocationScoutState())
    private val msg = storeOf(UIMessage(introMsg))
    val stateFlow = state.flow
    val stateNow get() = state.now
    val messageFlow = msg.flow

    private var count = 0

    fun setWebsite(value: String) {
        state.set { it.copy(website = value) }
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

    fun setEdit(edit: LocationEdit) {
        state.set { it.copy(edit = edit) }
    }

    fun searchQuery() {
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
        msg.set("Searching OSM: ${state.now.query}")
        scope.launch {
            val place = osm.readPlaces(query, bounds)?.mapNotNull { it.toPlace().takeIf {
                p -> p.geoPoint?.let { gp -> gp.distanceTo(geo.stateNow.center) < 100.kilometers } ?: false
            } }
                ?.firstOrNull()
            if (place == null) {
                msg.set("We couldn't find anything.")
                return@launch
            }
            place.geoPoint?.let {
                geo.panMap(PanPoint(point = it, zoom = 15f))
            }

            val edit = place.toEdit().mergeRight(state.now.edit)
            state.set { it.copy(edit = edit) }
            msg.set("Is this what you are looking for?")

//            state.set { it.copy(places = places) }

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

    fun readWebsite() {
        val website = state.now.website.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            msg.set("Reading the link, this will take a minute.")
            val edit = api.parseLocation(UrlParseRequest(website))?.mergeLeft(state.now.edit) ?: return@launch
            msg.set("Does this information look correct?")
            state.set { it.copy(edit = edit) }
        }
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
        val edit = place.toEdit().mergeRight(state.now.edit)
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

    fun coldRead() {
        val website = state.now.website.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            msg.set("Reading the link, this will take a minute.")
            val edit = api.parseLocation(UrlParseRequest(website))?.mergeLeft(state.now.edit) ?: return@launch
            val query = edit.address ?: edit.name
            state.set { it.copy(edit = edit, query = query ?: state.now.query ) }
            if (query != null) {
                searchQuery()
            } else {
                msg.set("We couldn't read the address or name from the website.")
            }
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

