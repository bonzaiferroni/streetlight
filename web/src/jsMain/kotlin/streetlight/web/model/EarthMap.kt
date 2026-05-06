package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.Post
import streetlight.web.EarthMapRoute
import streetlight.web.io.getDataOrNull
import streetlight.web.io.handleResponse
import streetlight.web.ui.ViewModel

class EarthMap(
    override val app: Streetlight,
    private val scope: CoroutineScope
): ViewModel {

    private val state = storeOf(EarthMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val postsFlow = stateFlow.mapDistinct { it.posts }

    init {
        scope.launch {
            app.portal.routeFlow.collect { route ->
                when (route) {
                    is EarthMapRoute -> {
                        val galaxy = route.galaxySlug?.let {
                            api.readGalaxySlug(it).handleResponse(toaster::toast)
                        }
                        val posts = galaxy?.let {
                            api.readPosts(it.galaxyId).getDataOrNull()
                        }
                        state.set { it.copy(galaxy = galaxy, posts = posts) }
                    }
                }
            }
        }
    }
}

data class EarthMapState(
    val galaxy: Galaxy? = null,
    val posts: List<Post>? = null,
)