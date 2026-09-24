package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.toDataOr
import koala.dom.MessageStore
import koala.model.dedup
import kampfire.model.tapOf
import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.EventLocation
import streetlight.model.data.Entity
import streetlight.model.data.Galaxy
import streetlight.model.data.PostEdit
import streetlight.model.data.PostId
import streetlight.model.data.PostType
import streetlight.web.io.ApiClient

/**
 * Posts an event to [galaxy] in stages: find or create its location with [LocationScout], then find or create
 * the event.
 */
class EventScout(
    val galaxy: Galaxy?,
    private val editor: EventEditor,
    private val locationScout: LocationScout,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
    siteConfig: SiteConfig,
) {
    private val state = storeOf(EventScoutState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    val postMessage = MessageStore()
    val postAndResetState = siteConfig.postAndResetState

    val postFlow = stateFlow.dedup { it.post }
    val stage = state.mutableTapOf({ it.stage }) { copy(stage = it) }
    val query = state.mutableTapOf({ it.query }) { copy(query = it) }
    val event = state.mutableTapOf({ it.event }) { copy(event = it) }
    val queryEventsFlow = state.tapOf { it.queryEvents }

    init {
        locationScout.stageField.reactIn(scope) { locationStage ->
            val stage = when (locationStage) {
                LocationScoutStage.Search -> EventScoutStage.LocationSearch
                LocationScoutStage.Edit -> EventScoutStage.LocationEdit
                LocationScoutStage.Review -> EventScoutStage.EventSearch
            }
            state.set { copy(stage = stage) }
        }
        locationScout.locationState.reactIn(scope) { location ->
            editor.setLocationId(location?.locationId)
            if (location != null) {
                state.set { copy(stage = EventScoutStage.EventSearch)}
            }
        }
    }

    // fun setStage(value: EventScoutStage) = state.set { it. }
    // fun setEvent(value: EventLocation?) = state.set { copy(event = value) }

    /** Starts a new event titled with the query. */
    fun create() {
        editor.titleState.set(stateNow.query)
        state.set { copy(stage = EventScoutStage.EventEdit) }
    }

    fun review() {
        if (!editor.isEditValid()) return
        state.set { copy(stage = EventScoutStage.Post) }
    }

    fun submitLocation() {
        scope.launch {
            locationScout.submitLocation()
        }
    }

    /** Saves the event when it is new, then posts it to the galaxy when there is one. */
    fun post() {
        scope.launch {
            postMessage.deliverSending("Posting...")
            val (eventId, title) = when (val event = stateNow.event) {
                null -> editor.submitSuspend()?.let { it.eventId to it.title }
                else -> event.eventId to event.title
            } ?: return@launch

            val postId = galaxy?.let { galaxy ->
                val edit = PostEdit(null, galaxy.galaxyId, PostType.Event, eventId.value, null)
                api.post.createPost(edit).toDataOr(postMessage) { return@launch }.postId
            }
            when (postAndResetState.now) {
                true -> {
                    toaster.deliverSuccess("Posted! Ready for the next one.")
                    reset()
                }
                false -> {
                    toaster.deliverSuccess(galaxy?.let { "Posted to ${it.name}." } ?: "Posted $title.")
                    state.set { copy(postId = postId, isPosted = true) }
                }
            }
        }
    }

    /** Returns to the location search with nothing chosen. */
    fun reset() {
        state.set { EventScoutState() }
        editor.reset()
        locationScout.reset()
        postMessage.clear()
    }

    private fun addConstructionMarker(point: GeoPoint) {
//        geo.tempEntities(listOf(
//            IconEntity("here", SvgFile.Guitar, point)
//        ))
    }
}

data class EventScoutState(
    val post: Entity? = null,
    val stage: EventScoutStage = EventScoutStage.LocationSearch,
    val query: String = "",
    val event: EventLocation? = null,
    val queryEvents: List<EventLocation> = emptyList(),
    val postId: PostId? = null,
    val isPosted: Boolean = false,
)

enum class EventScoutStage(label: String? = null) : Labeled {
    LocationSearch("Find a location"),
    LocationEdit,
    EventSearch("Add an event"),
    EventEdit,
    Post;

    override val label = label ?: name
}