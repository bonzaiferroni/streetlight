package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.Location
import streetlight.model.data.NewGalaxyLocationPost
import streetlight.model.data.NewLocationPost
import streetlight.web.ui.ViewModel

class LocationScout(
    private val scope: CoroutineScope,
    override val app: Streetlight,
    val finder: LocationFinder,
    val galaxyId: GalaxyId,
): ViewModel {

    private var location: Location? = null
    private val state = storeOf(LocationScoutState(setOf(galaxyId)))
    val stateFlow = state.flow
    val stateNow get() = state.now

    init {
        scope.launch {
            finder.stateFlow.mapDistinct { it.location }.collect {
                location = it
            }
        }
    }

    fun setTitle(value: String) = state.set { it.copy(title = value) }

    fun setText(value: String) = state.set { it.copy(text = value) }

    fun addGalaxyId(galaxyId: GalaxyId) = state.set { it.copy(galaxyIds = it.galaxyIds + galaxyId) }

    fun removeGalaxyId(galaxyId: GalaxyId) = state.set { it.copy(galaxyIds = it.galaxyIds - galaxyId) }

    fun createPost() {
        val location = location ?: error("location not found")
        val title = stateNow.title
        val text = stateNow.text
        scope.launch {
            val post = NewLocationPost(location.locationId, title, text)
            val postId = api.postLocation(post) ?: error("result not found")
            val galaxyPost = NewGalaxyLocationPost(postId, stateNow.galaxyIds.toList())
            val galaxyResult = api.postGalaxyLocation(galaxyPost)
            console.log(galaxyResult)
        }
    }
}

data class LocationScoutState(
    val galaxyIds: Set<GalaxyId>,
    val title: String? = null,
    val text: String? = null
)