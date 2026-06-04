package streetlight.web.model

import kampfire.model.getDataOrNull
import kampfire.model.handleResponse
import koala.model.Portal
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.web.EarthRoute
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
    val galaxiesFlow = stateFlow.mapDistinct { it.galaxies }
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val postsFlow = stateFlow.mapDistinct { it.posts ?: emptyList() }
    val postFlow = stateFlow.mapDistinct { it.post }

    init {
        scope.launch {
            launch {
                val galaxies = api.readTopGalaxies().getDataOrNull() ?: return@launch
                state.set { it.copy(galaxies = galaxies) }
            }
            launch {
                portal.routeFlow.collect { route ->
                    when (route) {
                        is EarthRoute -> {
                            val galaxy = route.slug?.let {
                                api.readGalaxy(it).handleResponse(toaster::toast)
                            }
                            val posts = galaxy?.let {
                                api.readPosts(it.galaxyId).handleResponse(toaster::toast)
                            }
                            state.set { it.copy(galaxy = galaxy, posts = posts, post = null) }
                        }
                    }
                }
            }
        }
    }

    fun setPost(value: GalaxyPost?) = state.set { it.copy(post = value) }
}

data class EarthMapState(
    val galaxy: Galaxy? = null,
    val posts: List<GalaxyPost>? = null,
    val galaxies: List<Galaxy> = emptyList(),
    val post: GalaxyPost? = null,
)
