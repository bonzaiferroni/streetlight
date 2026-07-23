package koala.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.handleResponse
import koala.utils.launch
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
    val messenger: Messenger,
) {
    private val state = storeOf(RouteInflatorState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    init {
        scope.launch(RouteInflator::class) {
            portal.stateFlow.filter { !it.isInitialRoute || !it.route.screen.hasShell }.map { it.route }.collectLatest { route ->
                // console.log("inflating route")
                state.set { copy(delivery = null) }
                val content = fetcher.fetchContent(route).handleResponse(messenger)
                val delivery = RouteDelivery(route, content)
                // console.log("inflate content: ${content != null}")
                state.set { copy(delivery = delivery) }
            }
        }
    }

    suspend inline fun <reified T: FetcherContent> contentFor(route: AppRoute): T {
        val content = stateFlow.first { it.delivery?.route == route }.delivery?.content as? T
        if (content == null) {
            messenger.deliver("Something went wrong")
            error("no content for route: $route")
        }
        return content
    }
}

data class RouteInflatorState(
    val delivery: RouteDelivery? = null,
    // val isInflating: Boolean = false,
)

data class RouteDelivery(
    val route: AppRoute,
    val content: FetcherContent?
)

interface ContentFetcher {
    suspend fun fetchContent(route: AppRoute): Outcome<FetcherContent>
}