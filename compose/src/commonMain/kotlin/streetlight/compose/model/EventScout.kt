package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.model.MessageStore
import koala.model.Toaster
import kotlinx.coroutines.launch
import streetlight.compose.io.OsmClient
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationEntity
import streetlight.model.data.PostEdit
import streetlight.model.data.PostType
import streetlight.model.io.ApiClient

/**
 * Finds or starts an event's location with its [finder], writing a new one in its [locationEditor], then starts the
 * event in its [eventEditor], to post it to [galaxy], when given. The focus is what the star is working on, and its
 * stage follows from it. The cousin of the web's `EventScout`.
 */
class EventScout(
    val galaxy: Galaxy?,
    private val api: ApiClient,
    osm: OsmClient,
    geoMap: GeoMap,
    config: AppConfig,
    private val toaster: Toaster,
) : ViewModel() {
    private val store = storeOf(EventScoutState())

    val finder = LocationFinder(viewModelScope, galaxy, api, osm, geoMap, toaster, ::focus)
    val locationEditor = LocationEditor(api)
    val eventEditor = EventEditor(api)
    val focusState = store.tapOf { it.focus }
    val stageState = store.tapOf { it.focus.toStage() }
    val eventQueryState = store.mutableTapOf({ it.eventQuery }) { copy(eventQuery = it) }
    val postMessage = MessageStore()
    val isPostedState = store.tapOf { it.isPosted }
    val postModeState = config.eventPostModeState
    val feedModeState = config.feedModeState

    /** Works on [location]: finds its event when it is one of Streetlight's, or starts editing it when it is new. */
    fun focus(location: LocationEntity) = when (location) {
        is Location -> searchEventsAt(location)
        is LocationEdit -> {
            locationEditor.setEdit(location)
            store.set { copy(focus = EventScoutFocus.LocationEdit) }
        }
    }

    /** Saves the new location, then goes on to find its event. */
    fun submitLocation() {
        viewModelScope.launch {
            val location = locationEditor.saveLocation() ?: return@launch
            searchEventsAt(location)
        }
    }

    /** Starts a new event at the location, titled with the event query. */
    fun createEvent() {
        val location = store.now.focus?.location ?: return
        val title = store.now.eventQuery.takeIf { it.isNotBlank() }
        eventEditor.setEdit(newEventEdit(title, location.locationId))
        store.set { copy(focus = EventScoutFocus.EventEdit(location)) }
    }

    /** Reviews the new event, once its edit is valid. */
    fun review() {
        val focus = store.now.focus as? EventScoutFocus.EventEdit ?: return
        if (!eventEditor.isEditValid()) return
        store.set { copy(focus = EventScoutFocus.Review(focus.location)) }
    }

    fun resumeEdit() {
        val location = store.now.focus?.location ?: return
        store.set { copy(focus = EventScoutFocus.EventEdit(location)) }
    }

    fun searchEvents() {
        val location = store.now.focus?.location ?: return
        searchEventsAt(location)
    }

    /** Returns to the location search, keeping what was found. */
    fun searchLocations() = store.set { copy(focus = null) }

    /** Saves the event, posts it to the galaxy when there is one, and goes on by the post mode. */
    fun post() {
        val focus = store.now.focus as? EventScoutFocus.Review ?: return
        postMessage.clear()
        viewModelScope.launch {
            val event = eventEditor.saveEvent() ?: return@launch
            galaxy?.let { galaxy ->
                postMessage.deliverSending("Posting...")
                val edit = PostEdit(null, galaxy.galaxyId, PostType.Event, event.eventId.value, null)
                api.post.createPost(edit).toDataOr(postMessage) { return@launch }
            }
            when (postModeState.now) {
                EventPostMode.ResetLocation -> {
                    toaster.deliverSuccess("Posted! Ready for the next one.")
                    reset()
                }
                EventPostMode.ResetEvent -> {
                    toaster.deliverSuccess("Posted! Ready for another event here.")
                    resetEvent(focus.location)
                }
                EventPostMode.Return -> {
                    toaster.deliverSuccess(galaxy?.let { "Posted to ${it.name}." } ?: "Posted ${event.title}.")
                    store.set { copy(isPosted = true) }
                }
            }
        }
    }

    /** Returns to the location search with nothing chosen or found. */
    fun reset() {
        store.set { EventScoutState() }
        finder.reset()
        locationEditor.setEdit(LocationEdit())
        eventEditor.setEdit(newEventEdit())
        postMessage.clear()
    }

    private fun searchEventsAt(location: Location) = store.set { copy(focus = EventScoutFocus.EventSearch(location)) }

    private fun resetEvent(location: Location) {
        store.set { EventScoutState(focus = EventScoutFocus.EventSearch(location)) }
        eventEditor.setEdit(newEventEdit())
        postMessage.clear()
    }
}

data class EventScoutState(
    val focus: EventScoutFocus? = null,
    val eventQuery: String = "",
    val isPosted: Boolean = false,
)

sealed interface EventScoutFocus {
    /** The location the event is at, once there is one. */
    val location: Location? get() = null

    /** A new location, its details being written in the scout's location editor. */
    data object LocationEdit : EventScoutFocus

    /** The search for an event at [location]. */
    data class EventSearch(override val location: Location) : EventScoutFocus

    /** A new event at [location], its details being written in the scout's event editor. */
    data class EventEdit(override val location: Location) : EventScoutFocus

    /** A new event at [location], as written, ready to post. */
    data class Review(override val location: Location) : EventScoutFocus
}

/** The stages of an [EventScout], in order, each with its [label], as the web's heading stages. */
enum class EventScoutStage(val label: String) {
    Location("Find a location"),
    Event("Add an event"),
    Post("Post"),
}

/** The stage of this focus: the location's while it is found or written, the event's while it is found or written. */
fun EventScoutFocus?.toStage() = when (this) {
    null, EventScoutFocus.LocationEdit -> EventScoutStage.Location
    is EventScoutFocus.EventSearch, is EventScoutFocus.EventEdit -> EventScoutStage.Event
    is EventScoutFocus.Review -> EventScoutStage.Post
}
