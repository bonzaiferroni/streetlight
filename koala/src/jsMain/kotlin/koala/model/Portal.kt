package koala.model

import koala.html.AppRoute
import koala.html.AppScreen
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.MANUAL
import org.w3c.dom.ScrollRestoration
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.url.URL

class Portal(
    initialRoute: AppRoute,
    val screens: List<AppScreen>,
    private val scope: CoroutineScope
) {
    private val state = storeOf(PortalState(initialRoute))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val screenFlow = stateFlow.mapDistinct { it.route.screen }
    val routeFlow = stateFlow.mapDistinct { it.route }

    private var backstack: List<Navigation> = emptyList()

    var sitePath
        get() = window.location.pathname
        set(value: String) {
            if (window.location.pathname.drop(1) == value) return
            val href = "${window.location.origin}$value${window.location.search}"
            window.history.pushState(null, "", href)
        }

    init {
        // keep scroll from jumping on back press
        window.history.scrollRestoration = ScrollRestoration.MANUAL

        // initialize route from current address
        val route = routeOf(window.location.pathname)
        go(route ?: initialRoute)

        fun handleRoute(href: String) {
            val sitePath = if (href.startsWith("/")) href else URL(href).pathname

            val route = routeOf(sitePath) ?: return
            if (route == stateNow.route) return
            go(route)
        }

        document.addEventListener("click", { event ->
            val event = event as? MouseEvent ?: return@addEventListener
            val anchor = (event.target as? HTMLElement)?.closest("a") as? HTMLAnchorElement ?: return@addEventListener
            val modifiedClick = event.ctrlKey || event.metaKey || event.shiftKey

            // Only intercept local paths, let external links sail free
            if (anchor.hostname == window.location.hostname && anchor.target != "_blank" && !modifiedClick) {
                event.preventDefault()
                val href = anchor.getAttribute("href") ?: return@addEventListener
                handleRoute(href)
            }
        })

        window.addEventListener("popstate", {
            goBack(window.location.pathname)
        })
    }

    inline fun <reified T: AppRoute> routeFlowOf(): Flow<T> {
        return routeFlow.mapDistinct {
            try {
                it as? T
            } catch(e: Exception) {
                console.log("narr!")
                console.log(e)
                throw(e)
            }
        }.filterNotNull()
    }

    fun go(route: AppRoute) {
        val navigation = Navigation(route, 0.0)
        go(navigation, backstack + Navigation(stateNow.route, window.scrollY))
    }

    fun goBack(sitePath: String? = null) {
        val (jumps, route) = sitePath?.let {
            backstack.asReversed()
                .withIndex()
                .firstOrNull { (_, value) -> value.route.toSitePath() == it }
        } ?: backstack.lastOrNull()?.let { IndexedValue(1, it) } ?: return
        go(route, backstack.dropLast(jumps))
    }

    private fun go(navigation: Navigation, backstack: List<Navigation>) {
        val route = navigation.route
        this.backstack = backstack
        state.set { it.copy(
            route = route,
            title = route.title,
            canGoBack = backstack.isNotEmpty(),
            initialScroll = navigation.initialScroll
        )}
        sitePath = route.toSitePath()
    }

    private fun routeOf(hashPath: String): AppRoute? {
        return AppRoute.routeOf(hashPath, screens)
    }
}

data class PortalState(
    val route: AppRoute,
    val title: String? = null,
    val canGoBack: Boolean = false,
    val initialScroll: Double = 0.0,
)

private data class Navigation(
    val route: AppRoute,
    val initialScroll: Double,
)