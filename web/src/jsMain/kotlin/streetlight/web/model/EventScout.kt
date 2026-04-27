@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.distanceTo
import kampfire.model.kilometers
import koala.SvgFile
import koala.dom.UIMessage
import koala.dom.set
import koala.model.PanPoint
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.EventPost
import streetlight.model.data.EventPostEdit
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Place
import streetlight.model.data.Post
import streetlight.model.data.UrlParseRequest
import streetlight.model.data.mergeLeft
import streetlight.model.data.mergeRight
import streetlight.model.data.toEdit
import streetlight.model.external.toPlace
import streetlight.web.ui.ViewModel

class EventScout(
    override val app: Streetlight,
    val scope: CoroutineScope,
    val galaxy: Galaxy,
): ViewModel {
    private val state = storeOf(EventScoutState(blankEvent))
    val stateFlow = state.flow
    val stateNow get() = state.now

    private val msg = storeOf(UIMessage(introMsg))
    val messageFlow = msg.flow
    val validEventFlow = stateFlow.mapDistinct { UIMessage(it.eventEdit.invalidMessage ?: "Looks good") }

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

    fun setWebsite(value: String) {
        state.set { it.copy(website = value) }
    }

    fun setEventLink(value: String) {
        state.set { it.copy(eventEdit = state.now.eventEdit.copy(link = value))}
    }

    fun setLocation(value: Location?) {
        val eventEdit = state.now.eventEdit.copy(locationId = value?.locationId)
        state.set { it.copy(location = value, eventEdit = eventEdit) }
    }

    fun setEdit(value: LocationEdit) {
        state.set { it.copy(locationEdit = value) }
    }

    fun setEventEdit(value: EventEdit) {
        state.set { it.copy(eventEdit = value) }
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

    fun readLocationWebsite() {
        val website = state.now.website.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            msg.set("Reading the link, this will take a minute.")
            when (val response = api.parseLocation(UrlParseRequest(website))) {
                is Ok -> {
                    val edit = response.data.mergeLeft(state.now.locationEdit)
                    state.set { it.copy(locationEdit = edit) }
                    val message = response.message ?: "Does this information look correct?"
                    msg.set(message)
                }
                is Problem -> {
                    msg.set(response.message)
                }
                null -> {
                    msg.set("Something went wrong.")
                }
            }
        }
    }

    fun readEventWebsite() {
        val website = state.now.eventEdit.link?.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            msg.set("Reading the link, this will take a minute.")
            val response = api.parseSingleEvent(UrlParseRequest(website))?.data
            if (response == null) {
                msg.set("We were unable to read the link.")
                return@launch
            }
            val event = response.mergeRight(state.now.eventEdit)
            msg.set("Does this information look correct?")
            state.set { it.copy(eventEdit = event) }
        }
    }

    fun postLocation() {
        val edit = state.now.locationEdit?.takeIf { it.isValid } ?: return
        msg.set("Posting ${edit.name}...")
        scope.launch {
            val location = api.createOrEditLocation(edit)
            if (location == null) {
                msg.set("Something went wrong")
                return@launch
            }
            state.set { it.copy(location = location, eventEdit = it.eventEdit.copy(
                locationId = location.locationId
            )) }
            msg.set("Posted. You can now add events to ${location.name}.")
            geo.tempEntities(null)
        }
    }

    fun postEvent() {
        val edit = state.now.eventEdit.takeIf { it.isValid } ?: return
        msg.set("Posting ${edit.title}...")
        scope.launch {
            val event = api.createOrEditEvent(edit)?.payload
            if (event == null) {
                msg.set("Something went wrong")
                return@launch
            }
            state.set { it.copy(event = event) }
            postToGalaxy(galaxy.galaxyId)
            msg.set("Posted.")
        }
    }

    fun postToGalaxy(galaxyId: GalaxyId) {
        val event = state.now.event ?: return
        scope.launch {
            val postId = api.createPost(EventPostEdit(
                galaxyId = galaxyId,
                eventId = event.eventId,
            ))
            if (postId != null) {
                val post = api.readPost(postId) ?: return@launch
                app.streetMap.addPosts(listOf(post))
                state.set { it.copy(posts = it.posts + post)}
            }
        }
    }

    fun reset() {
        state.set { EventScoutState(blankEvent) }
    }

    fun resetEvent() {
        val location = state.now.location ?: return
        val edit = blankEvent.copy(locationId = location.locationId)
        state.set { it.copy(eventEdit = edit, event = null, location = location) }
    }

    private fun addConstructionMarker(point: GeoPoint) {
        geo.tempEntities(listOf(
            IconEntity("here", SvgFile.Guitar, point)
        ))
    }
}

data class EventScoutState(
    val eventEdit: EventEdit,
    val query: String = "",
    val website: String = "",
    val locations: List<Location> = emptyList(),
    val point: GeoPoint? = null,
    val locationEdit: LocationEdit? = null,
    val location: Location? = null,
    val event: Event? = null,
    val posts: List<Post> = emptyList()
)

private const val introMsg = "Where will the event be held? Search for a location or find one on the map."

object BrowserTime {
    val timeZoneId by lazy { js("Intl.DateTimeFormat().resolvedOptions().timeZone") as String }
}

private val blankEvent get() = EventEdit(
    timeZoneId = BrowserTime.timeZoneId,
)