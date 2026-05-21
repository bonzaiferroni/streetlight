package streetlight.web.model

import kampfire.model.handleResponse
import koala.model.Portal
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.Post
import streetlight.web.EarthMapRoute
import streetlight.web.io.ApiClient

class EarthMap(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val portal: Portal,
    private val toaster: Toaster,
) {

    private val state = storeOf(EarthMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val postsFlow = stateFlow.mapDistinct { it.posts }

    init {
        scope.launch {
            portal.routeFlow.collect { route ->
                when (route) {
                    is EarthMapRoute -> {
                        val galaxy = route.galaxySlug?.let {
                            api.readGalaxy(it).handleResponse(toaster::toast)
                        }
                        val posts = galaxy?.let {
                            api.readPosts(it.galaxyId).handleResponse(toaster::toast)
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