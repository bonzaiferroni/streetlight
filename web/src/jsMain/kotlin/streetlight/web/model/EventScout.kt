package streetlight.web.model

import kampfire.model.Labeled
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.Post

class EventScout(
    private val galaxy: Galaxy,
    private val editor: EventEditor,
    private val location: LocationScout,
    private val scope: CoroutineScope,
) {
    private val state = storeOf(EventScoutState())
    val stateFlow = state.flow
    val stateNow get() = state.now

    val postFlow = stateFlow.mapDistinct { it.post }
    val stageFlow = stateFlow.mapDistinct { it.stage }

    init {
        scope.launch {
            location.stageFlow.collect { locationStage ->
                val stage = when (locationStage) {
                    LocationScoutStage.Search -> EventScoutStage.LocationSearch
                    LocationScoutStage.Edit -> EventScoutStage.LocationEdit
                    LocationScoutStage.Post -> EventScoutStage.EventSearch
                }
                state.set { it.copy(stage = stage) }
            }
        }
    }

    fun setStage(value: EventScoutStage) = state.set { it.copy(stage = value) }

}

data class EventScoutState(
    val post: Post? = null,
    val stage: EventScoutStage = EventScoutStage.LocationSearch,
)

enum class EventScoutStage(label: String? = null): Labeled {
    LocationSearch("Location Search"),
    LocationEdit("Location Edit"),
    EventSearch("Event Search"),
    EventEdit("Event Edit"),
    Post;

    override val label = label ?: name
}