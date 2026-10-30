package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.api.Slug
import kampfire.model.Ok
import kampfire.model.toDataOr
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Tap
import kampfire.model.storeOf
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.io.ApiClient

/** Reads the content each route shows, one branch per route. The cousin of the web's `AppContentFetcher`. */
class Inflator(private val api: ApiClient) {
    /** The content of [route], read from the server. */
    suspend fun inflate(route: AppRoute): Outcome<Any> = when (route) {
        HomeRoute -> api.content.readHomeContent(null)
        GalaxyFeedRoute -> api.content.readGalaxyFeedContent(null)
        StarFeedRoute -> api.content.readStarFeedContent()
        is GalaxyRoute -> api.galaxy.readGalaxyContent(route.slug)
        is GalaxyMapRoute -> api.galaxy.readGalaxyContent(route.slug)
        is EventRoute -> api.event.readEventSlug(route.slug)
        is LocationRoute -> api.location.readLocationContent(route.slug)
        // the earth reads its content as the camera moves, the dashboard each tab its own, and the config is local, so
        // none inflates
        EarthRoute, is StarDashRoute, AppConfigRoute -> Ok(Unit)
        StarConfigRoute -> api.star.readAccount()
        is EventScoutRoute -> readPostGalaxy(route.slug)
        is LocationScoutRoute -> readPostGalaxy(route.slug)
        is MediaForgeRoute -> readPostGalaxy(route.slug)
    }

    /** The galaxy of [slug] a new post goes to, or none without one. */
    private suspend fun readPostGalaxy(slug: Slug?): Outcome<PostGalaxy> = when (slug) {
        null -> Ok(PostGalaxy(null))
        else -> api.galaxy.readGalaxy(slug).toDataOr { return it }.let { Ok(PostGalaxy(it)) }
    }
}

/** The content of a screen that creates a post: the [galaxy] the post goes to, or `null` for none. */
data class PostGalaxy(val galaxy: Galaxy?)

/** The content of a route as it arrives: on its way, ready, or kept by a problem. */
sealed interface Inflation {
    data object Loading : Inflation
    data class Ready(val content: Any) : Inflation
    data class Problem(val message: String) : Inflation
}

/**
 * The inflation of [route], read by the [inflator] when it is built and kept while the route is on the back stack, so
 * a return to the route shows its content without reading it again.
 */
class InflationModel(route: AppRoute, inflator: Inflator) : ViewModel() {
    private val store = storeOf<Inflation>(Inflation.Loading)

    val inflationState: Tap<Inflation> = store

    init {
        viewModelScope.launch {
            store.set(
                when (val outcome = inflator.inflate(route)) {
                    is Ok -> Inflation.Ready(outcome.data)
                    is Problem -> Inflation.Problem(outcome.message)
                }
            )
        }
    }
}
