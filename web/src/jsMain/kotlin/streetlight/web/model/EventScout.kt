package streetlight.web.model

import kampfire.api.Slug
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.handleOutcome
import koala.dom.MessageStore
import koala.model.mapDistinct
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

    val postFlow = stateFlow.mapDistinct { it.post }
    val stageFlow = stateFlow.mapDistinct { it.stage }
    val queryFlow = stateFlow.mapDistinct { it.query }
    val eventFlow = stateFlow.mapDistinct { it.event }
    val queryEventsFlow = stateFlow.mapDistinct { it.queryEvents }

    init {
        scope.launch {
            launch {
                locationScout.stageFlow.collect { locationStage ->
                    val stage = when (locationStage) {
                        LocationScoutStage.Search -> EventScoutStage.LocationSearch
                        LocationScoutStage.Edit -> EventScoutStage.LocationEdit
                        LocationScoutStage.Post -> EventScoutStage.EventSearch
                    }
                    state.set { it.copy(stage = stage) }
                }
            }
            launch {
                locationScout.locationFlow.collect { location ->
                    editor.setLocationId(location?.locationId)
                    if (location != null) {
                        state.set { it.copy(stage = EventScoutStage.EventSearch)}
                    }
                }
            }
        }
    }

    fun setStage(value: EventScoutStage) = state.set { it.copy(stage = value) }
    fun setQuery(value: String) = state.set { it.copy(query = value) }
    fun setEvent(value: EventLocation?) = state.set { it.copy(event = value) }

    fun create() {
        editor.setTitle(stateNow.query)
        state.set { it.copy(stage = EventScoutStage.EventEdit) }
    }

    fun review() {
        if (!editor.isEditValid()) return
        state.set { it.copy(stage = EventScoutStage.Post) }
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
            api.createPost(edit).handleOutcome(postMessage::set) { post ->
                state.set { it.copy(postId = post.postId) }
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