package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.GalaxyId
import streetlight.model.data.Location
import streetlight.model.data.LocationPostEdit
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
            val post = LocationPostEdit(null, galaxyId, location.locationId, text)
            val postId = api.postLocation(post) ?: error("result not found")
            state.set { it.copy(postId = postId) }
        }
    }
}

data class LocationScoutState(
    val text: String? = null,
    val postId: PostId? = null
)