@file:OptIn(ExperimentalWasmJsInterop::class)

package koala.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.core.queryAttribute
import koala.modifier.KoalaBody
import koala.modifier.setAttribute
import koala.html.AppRoute
import koala.html.AppScreen
import koala.modifier.Attribute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import web.dom.document
import web.events.addEventListener
import web.history.POP_STATE
import web.history.PopStateEvent
import web.history.ScrollRestoration
import web.history.history
import web.history.manual
import web.html.HTMLAnchorElement
import web.html.HTMLElement
import web.pointer.CLICK
import web.pointer.PointerEvent
import web.url.URL
import web.window.Window
import web.window.WindowTarget
import web.window._blank
import web.window.window
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The current route of the app, kept in step with the address bar.
 *
 * It reads the route from the address on load and on browser navigation, and intercepts clicks on local links.
 */
class Portal(
    initialRoute: AppRoute,
    val screens: List<AppScreen>,
) {
    private val state = storeOf(PortalState(queryRoute() ?: routeOf(window.location.pathname) ?: initialRoute))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val routeState = state.tapOf { it.route }
    val screenState = state.tapOf(
        applyFlow = { states ->
            states.dedupBy({ if (it.route.screen.retainWithinScreen) it.route.screen else it }) { it.route.screen }
        }
    ) { it.route.screen }

    private var backstack: List<Navigation> = emptyList()
    private var isWrecked = false

    var sitePath
        get() = window.location.pathname
        set(value: String) {
            if (window.location.pathname == value) return
            val href = "${window.location.origin}$value${window.location.search}"
            history.pushState(null, "", href)
        }

    init {
        // hack to keep scroll from jumping on back press
        // known issue: this prevents scroll restoration on refresh
        history.scrollRestoration = ScrollRestoration.manual

        fun handleRoute(href: String, isClick: Boolean) {
            val href = window.prefixContext(href)
            val sitePath = if (href.startsWith("/")) href else URL(href).pathname

            val route = routeOf(sitePath) ?: return
            if (route == stateNow.route) {
                if (isClick) {
                    refresh()
                }
                return
            }
            go(route)
        }

        document.addEventListener(PointerEvent.CLICK, { event ->
            val anchor = (event.target as? HTMLElement)?.closest("a") as? HTMLAnchorElement ?: return@addEventListener
            val modifiedClick = event.ctrlKey || event.metaKey || event.shiftKey

            // Only intercept local paths, let external links sail free
            if (anchor.hostname == window.location.hostname && anchor.target != WindowTarget._blank && !modifiedClick && !isWrecked) {
                event.preventDefault()
                val href = anchor.getAttribute("href") ?: return@addEventListener
                handleRoute(href, true)
            }
        })

        window.addEventListener(PopStateEvent.POP_STATE, {
            if (isWrecked) {
                window.location.reload()
                return@addEventListener
            }
            val isSuccess = goBack(window.location.pathname)
            if (!isSuccess) {
                handleRoute(window.location.pathname, false)
            }
        })
    }

    /** A flow of the routes of type [T]. */
    inline fun <reified T : AppRoute> routeFlowOf(emitDistinct: Boolean = true): Flow<T> {
        val base = stateFlow.mapNotNull { it.route as? T }

        return if (emitDistinct) base.distinctUntilChanged() else base
    }

    /** Navigates to [route], remembering the current one and its scroll position. */
    fun go(route: AppRoute) {
        val navigation = Navigation(route, 0.0)
        go(navigation, backstack + Navigation(stateNow.route, window.scrollY))
    }

    /** Returns to the last route, or to the latest one at [sitePath], returning whether there was one. */
    fun goBack(sitePath: String? = null): Boolean {
        val (index, route) = when (sitePath) {
            null -> backstack.lastOrNull()?.let { IndexedValue(0, it) } ?: return false
            else -> backstack.asReversed().withIndex()
                .firstOrNull { (_, value) -> value.route.toRelativePath() == sitePath }
                ?: return false
        }
        go(route, backstack.dropLast(index + 1))
        return true
    }

    /** Rebuilds the current route. */
    fun refresh() {
        console.log("refreshing")
        state.set { copy(refreshedAt = Clock.System.now(), isInitialRoute = false) }
    }

    /** Marks the app as broken, so later navigation reloads the page. */
    fun notifyWrecked() {
        console.log("arr the ship wrecked")
        isWrecked = true
    }

    private fun go(navigation: Navigation, backstack: List<Navigation>) {
        val route = navigation.route
        console.log("setting route: ${route.screen}")
        this.backstack = backstack
        state.set { copy(
            route = route,
            canGoBack = backstack.isNotEmpty(),
            initialScrollY = navigation.initialScrollY,
            isInitialRoute = false,
            refreshedAt = Clock.System.now()
        )}
        sitePath = route.toRelativePath()

        document.body.setAttribute(KoalaBody.ScreenId.to(route.screen.screenId))
    }

    private fun routeOf(hashPath: String): AppRoute? {
        return AppRoute.routeOf(hashPath, screens)
    }

    private fun queryRoute(): AppRoute? {
        return document.queryAttribute(Attribute.RoutePath)?.let { routeOf(it) }
    }
}

// Portal's state should change if and only if the route changes
/** The current route, whether it is the one the page loaded with, and the scroll position to restore. */
data class PortalState(
    val route: AppRoute,
    val canGoBack: Boolean = false,
    val initialScrollY: Double = window.scrollY,
    val isInitialRoute: Boolean = true,
    val refreshedAt: Instant = Instant.DISTANT_PAST, // necessary for refreshing the same route
)

private data class Navigation(
    val route: AppRoute,
    val initialScrollY: Double,
)

private fun Window.prefixContext(href: String): String {
    if ('/' in href) return href
    val base = location.pathname.substringBeforeLast('/')
    return "$base/$href"
}