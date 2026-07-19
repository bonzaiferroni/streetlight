package streetlight.web.model

import kampfire.model.Outcome
import kampfire.model.PrintLnMessenger
import kampfire.model.handleResponse
import koala.dom.launch
import koala.html.AppRoute
import koala.model.Portal
import koala.model.storeOf
import koala.model.tap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import streetlight.model.data.StreetlightContent
import streetlight.model.ui.GalaxyRoute
import streetlight.web.io.ApiClient

class RouteInflator(
    private val scope: CoroutineScope, // appScope
    private val fetcher: ContentFetcher,
    private val portal: Portal,
    private val toaster: Toaster,
) {
    private val state = storeOf(RouteInflatorState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    init {
        scope.launch("RouteInflator") {
            portal.stateFlow.filter { !it.isInitialRoute }.map { it.route }.collectLatest { route ->
                console.log("inflating route")
                state.set { it.copy(content = null) }
                val content = fetcher.fetchContent(route).handleResponse(toaster)
                val routeContent = RouteContent(route, content)
                console.log("inflate content: ${content != null}")
                state.set { it.copy(content = routeContent)}
            }
        }
    }

    suspend inline fun <reified T: StreetlightContent> contentFor(route: AppRoute): T? =
        stateFlow.first { it.content?.route == route }.content?.content as? T
}

data class RouteInflatorState(
    val content: RouteContent? = null,
    // val isInflating: Boolean = false,
)

data class RouteContent(
    val route: AppRoute,
    val content: StreetlightContent?
)

