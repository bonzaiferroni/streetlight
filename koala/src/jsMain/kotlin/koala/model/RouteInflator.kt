package koala.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.handleResponse
import koala.dom.launch
import koala.html.AppRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RouteInflator(
    private val scope: CoroutineScope, // appScope
    private val fetcher: ContentFetcher,
    private val portal: Portal,
    private val messenger: Messenger,
) {
    private val state = storeOf(RouteInflatorState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    init {
        scope.launch(RouteInflator::class) {
            portal.stateFlow.filter { !it.isInitialRoute || !it.route.screen.hasShell }.map { it.route }.collectLatest { route ->
                // console.log("inflating route")
                state.setValue { it.copy(delivery = null) }
                val content = fetcher.fetchContent(route).handleResponse(messenger)
                val delivery = RouteDelivery(route, content)
                // console.log("inflate content: ${content != null}")
                state.setValue { it.copy(delivery = delivery)}
            }
        }
    }

    suspend inline fun <reified T: RouteContent> contentFor(route: AppRoute): T? =
        stateFlow.first { it.delivery?.route == route }.delivery?.content as? T
}

data class RouteInflatorState(
    val delivery: RouteDelivery? = null,
    // val isInflating: Boolean = false,
)

data class RouteDelivery(
    val route: AppRoute,
    val content: RouteContent?
)

interface ContentFetcher {
    suspend fun fetchContent(route: AppRoute): Outcome<RouteContent>
}