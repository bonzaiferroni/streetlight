package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.PostListing
import streetlight.web.EarthMapRoute
import streetlight.web.ui.ViewModel

class EarthMap(
    override val app: Streetlight,
    private val scope: CoroutineScope
): ViewModel {

    private val state = storeOf(EarthMapState())
    val stateFlow = state.flow
    val stateNow get() = state.now
    val galaxyFlow = stateFlow.mapDistinct { it.galaxy }
    val listingFlow = stateFlow.mapDistinct { it.listing }

    init {
        scope.launch {
            app.portal.routeFlow.collect { route ->
                when (route) {
                    is EarthMapRoute -> {
                        console.log(route.galaxySlug)
                        val galaxy = route.galaxySlug?.let {
                            api.readGalaxy(it)
                        }
                        val posts = galaxy?.let {
                            api.readPosts(it.galaxyId)
                        }
                        state.set { it.copy(galaxy = galaxy, listing = posts) }
                    }
                }
            }
        }
    }
}

data class EarthMapState(
    val galaxy: Galaxy? = null,
    val listing: PostListing? = null,
)