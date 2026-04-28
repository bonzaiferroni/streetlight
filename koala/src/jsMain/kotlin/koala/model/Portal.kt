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
        // window.addEventListener("hashchange", {
        //     val route = routeOf(hashPath) ?: return@addEventListener
        //     if (route == stateNow.route) return@addEventListener
        //     val backRoute = stateNow.backstack.lastOrNull()
        //     if (backRoute?.screen == route.screen) {
        //         // goBack()
        //         go(route) // td: maybe figure out
        //     } else {
        //         go(route)
        //     }
        // })

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
            // td: handle a jump further back than 1
            // When pushing new entries
            // history.pushState(historyIndex++, "", href)
            // When popstate fires
            // window.addEventListener("popstate") { event ->
            //     val state = (event as PopStateEvent).state as? Int
            //     // Compare state to your current index to know the distance
            // }
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

    inline fun <reified T: AppRoute> routeOrNullFlowOf(): Flow<T?> {
        return routeFlow.mapDistinct { it as? T }
    }

    fun go(route: AppRoute) {
        go(route, stateNow.backstack + stateNow.route)
    }

    fun goBack(sitePath: String? = null) {
        val (jumps, route) = sitePath?.let {
            stateNow.backstack.asReversed()
                .withIndex()
                .firstOrNull { (_, value) -> value.toSitePath() == it }
        } ?: stateNow.backstack.lastOrNull()?.let { IndexedValue(1, it) } ?: return
        go(route, stateNow.backstack.dropLast(jumps))
    }

    private fun go(route: AppRoute, backstack: List<AppRoute>) {
        state.set { it.copy(route = route, backstack = backstack, title = route.title)}
        sitePath = route.toSitePath()
    }

    private fun routeOf(hashPath: String): AppRoute? {
        return AppRoute.routeOf(hashPath, screens)
    }
}

data class PortalState(
    val route: AppRoute,
    val title: String? = null,
    val backstack: List<AppRoute> = emptyList()
) {
    val canGoBack get() = backstack.isNotEmpty()
}

private fun sitePathOf(address: String): String {
    if (address.startsWith("/")) return address.drop(1)
    return URL(address).pathname
}