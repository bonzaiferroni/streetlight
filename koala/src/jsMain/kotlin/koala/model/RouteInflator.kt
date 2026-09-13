package koala.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.storeOf
import kampfire.model.toDataOr
import koala.utils.launch
import koala.html.AppRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
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
    @PublishedApi internal var delivery: Deferred<RouteDelivery>? = null

    init {
        scope.launch(RouteInflator::class) {
            portal.stateFlow.filter { !it.isInitialRoute || !it.route.screen.hasShell }.map { it.route }.collectLatest { route ->
                delivery = async {
                    val content = fetcher.fetchContent(route).toDataOr(messenger) { awaitCancellation() }
                    RouteDelivery(route, content)
                }
            }
        }
    }

    suspend inline fun <reified T: FetcherContent> contentFor(route: AppRoute): T {
        val delivered = delivery?.await()
        val content = (if (delivered?.route == route) delivered.content else null) as? T
        if (content == null) {
            messenger.deliver("Something went wrong")
            error("no content for route: $route")
        }
        return content
    }

    fun clear() {
        delivery = null
    }
}

data class RouteDelivery(
    val route: AppRoute,
    val content: FetcherContent?
)

interface ContentFetcher {
    suspend fun fetchContent(route: AppRoute): Outcome<FetcherContent>
}