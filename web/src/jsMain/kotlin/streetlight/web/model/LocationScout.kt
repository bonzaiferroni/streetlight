package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Problem
import koala.dom.UIMessage
import koala.dom.set
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.GalaxyId
import streetlight.model.data.Location
import streetlight.model.data.LocationPostEdit
import streetlight.model.data.Post
import streetlight.model.data.PostId
import streetlight.web.ui.ViewModel

class LocationScout(
    private val scope: CoroutineScope,
    override val app: Streetlight,
    val finder: LocationFinder,
    val galaxyId: GalaxyId,
): ViewModel {

    private val initialState = LocationScoutState()
    private var location: Location? = null
    private val state = storeOf(initialState)
    val message = storeOf(UIMessage())
    val stateFlow = state.flow
    val stateNow get() = state.now

    init {
        scope.launch {
            finder.stateFlow.mapDistinct { it.location }.collect {
                location = it
            }
        }
    }


    fun setText(value: String) = state.set { it.copy(text = value) }

    fun reset() = state.set { initialState }

    fun createPost() {
        val location = location ?: error("location not found")
        val text = stateNow.text
        scope.launch {
            val edit = LocationPostEdit(null, galaxyId, location.locationId, text)
            when (val response = api.postLocation(edit)) {
                is Ok -> {
                    state.set { it.copy(post = response.data) }
                    message.set("Posted.")
                }
                is Problem, null -> {
                    message.set(response)
                }
            }
        }
    }
}

data class LocationScoutState(
    val text: String? = null,
    val post: Post? = null
)