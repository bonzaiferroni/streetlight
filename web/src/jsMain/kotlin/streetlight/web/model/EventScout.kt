package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.dedup
import koala.model.tapOf
import koala.model.mutableTapOf
import koala.model.reactIn
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.model.data.PostEdit
import streetlight.model.data.PostId
import streetlight.model.data.PostType
import streetlight.web.io.ApiClient

class EventScout(
    val galaxy: Galaxy,
    private val editor: EventEditor,
    private val locationScout: LocationScout,
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(EventScoutState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    val postMessage = MessageStore()

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
                LocationScoutStage.Post -> EventScoutStage.EventSearch
            }
            state.set { copy(stage = stage) }
        }
        locationScout.locationField.reactIn(scope) { location ->
            editor.setLocationId(location?.locationId)
            if (location != null) {
                state.set { copy(stage = EventScoutStage.EventSearch)}
            }
        }
    }

    // fun setStage(value: EventScoutStage) = state.set { it. }
    // fun setEvent(value: EventLocation?) = state.set { copy(event = value) }

    fun create() {
        editor.title.set(stateNow.query)
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

    fun post() {
        scope.launch {
            postMessage.set("Posting...", true)
            val eventId = when (val event = stateNow.event) {
                null -> editor.submitSuspend()?.eventId
                else -> event.eventId
            } ?: return@launch

            val edit = PostEdit(null, galaxy.galaxyId, PostType.Event, eventId.value, null)
            api.createPost(edit).handleResponse(postMessage) { post ->
                state.set { copy(postId = post.postId) }
            }
        }
    }

    private fun addConstructionMarker(point: GeoPoint) {
//        geo.tempEntities(listOf(
//            IconEntity("here", SvgFile.Guitar, point)
//        ))
    }
}

data class EventScoutState(
    val post: GalaxyPost? = null,
    val stage: EventScoutStage = EventScoutStage.LocationSearch,
    val query: String = "",
    val event: EventLocation? = null,
    val queryEvents: List<EventLocation> = emptyList(),
    val postId: PostId? = null,
)

enum class EventScoutStage(label: String? = null) : Labeled {
    LocationSearch("Find a location"),
    LocationEdit,
    EventSearch("Add an event"),
    EventEdit,
    Post;

    override val label = label ?: name
}