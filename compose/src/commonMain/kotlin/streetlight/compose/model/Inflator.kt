package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Tap
import kampfire.model.storeOf
import kotlinx.coroutines.launch
import streetlight.model.io.ApiClient

/** Reads the content each route shows, one branch per route. The cousin of the web's `AppContentFetcher`. */
class Inflator(private val api: ApiClient) {
    /** The content of [route], read from the server. */
    suspend fun inflate(route: AppRoute): Outcome<Any> = when (route) {
        HomeRoute -> api.content.readHomeContent(null)
        is GalaxyRoute -> api.galaxy.readGalaxyContent(route.slug)
        is EventRoute -> api.event.readEventSlug(route.slug)
        is LocationRoute -> api.location.readLocationContent(route.slug)
    }
}

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
