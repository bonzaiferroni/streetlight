@file:OptIn(ExperimentalWasmJsInterop::class)

package koala.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import koala.core.queryAttribute
import koala.modifier.KoalaBody
import koala.modifier.setAttribute
import koala.modifier.Attribute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.js.JsPlainObject
import web.dom.document
import web.events.CustomEvent
import web.events.EventType
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
import web.window.WindowTarget
import web.window._blank
import web.window.window
import kotlin.time.Clock
import kotlin.time.Instant
import koala.utility.dedupBy

/**
 * The current route of the app, kept in step with the address bar.
 *
 * It reads the route from the address on load and on browser navigation, and intercepts clicks on local links.
 */
class Portal(
    initialRoute: AppRoute,
    val screens: List<AppScreen>,
) {
    private val state = storeOf(PortalState(queryRoute() ?: routeOf(URL(window.location.href)) ?: initialRoute))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val routeState = state.tapOf { it.route }
    val screenState = state.tapOf(
        applyFlow = { states ->
            // a refresh rebuilds the view, and so does a route with a new screen key
            states.dedupBy({ it.route.screenKey to it.refreshedAt }) { it.route.screen }
        }
    ) { it.route.screen }

    private var backstack: List<Navigation> = emptyList()
    private var isWrecked = false

    val sitePath get() = window.location.run { pathname + search }

    init {
        // hack to keep scroll from jumping on back press
        // known issue: this prevents scroll restoration on refresh
        history.scrollRestoration = ScrollRestoration.manual

        fun handleRoute(route: AppRoute, isClick: Boolean) {
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

            // Only intercept local paths to a screen of this portal, let other links sail free
            if (anchor.hostname == window.location.hostname && anchor.target != WindowTarget._blank && !modifiedClick && !isWrecked) {
                val href = anchor.getAttribute("href") ?: return@addEventListener
                val route = routeOf(URL(href, window.location.href)) ?: return@addEventListener
                event.preventDefault()
                handleRoute(route, true)
            }
        })

        window.addEventListener(PopStateEvent.POP_STATE, {
            if (isWrecked) {
                window.location.reload()
                return@addEventListener
            }
            val isSuccess = goBack(sitePath)
            if (!isSuccess) {
                val route = routeOf(URL(window.location.href)) ?: return@addEventListener
                handleRoute(route, false)
            }
        })

        window.addEventListener(QUERY_PARAM_CHANGE, { event ->
            val url = URL(window.location.href)
            url.searchParams.set(event.detail.key, event.detail.value)
            val route = routeOf(url) ?: return@addEventListener
            if (route != stateNow.route) go(route)
        })
    }

    /** A flow of the routes of type [T]. */
    inline fun <reified T : AppRoute> routeFlowOf(emitDistinct: Boolean = true): Flow<T> {
        val base = stateFlow.mapNotNull { it.route as? T }

        return if (emitDistinct) base.distinctUntilChanged() else base
    }

    /**
     * Navigates to [route], remembering the current one and its scroll position.
     *
     * A [route] with the [AppRoute.screenKey] of the current one takes its place, in the backstack and in the browser's
     * history.
     */
    fun go(route: AppRoute) {
        val navigation = Navigation(route, 0.0)
        when (route.screenKey != stateNow.route.screenKey) {
            true -> go(navigation, backstack + Navigation(stateNow.route, window.scrollY), true)
            false -> go(navigation, backstack, false)
        }
    }

    /** Returns to the last route, or to the latest one at [sitePath], returning whether there was one. */
    fun goBack(sitePath: String? = null): Boolean {
        val (index, route) = when (sitePath) {
            null -> backstack.lastOrNull()?.let { IndexedValue(0, it) } ?: return false
            else -> backstack.asReversed().withIndex()
                .firstOrNull { (_, value) -> value.route.toRelativePath() == sitePath }
                ?: return false
        }
        go(route, backstack.dropLast(index + 1), false)
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

    private fun go(navigation: Navigation, backstack: List<Navigation>, appendToBackstack: Boolean) {
        val route = navigation.route
        console.log("setting route: ${route.screen}")
        this.backstack = backstack
        state.set { copy(
            route = route,
            canGoBack = backstack.isNotEmpty(),
            initialScrollY = navigation.initialScrollY,
            isInitialRoute = false,
            // going to the route already shown rebuilds it, as a refresh does
            refreshedAt = if (route == this.route) Clock.System.now() else refreshedAt,
        )}
        setSitePath(route.toRelativePath(), appendToBackstack)

        document.body.setAttribute(KoalaBody.ScreenId.to(route.screen.screenId))
    }

    private fun setSitePath(value: String, appendToBackstack: Boolean) {
        if (sitePath == value) return
        when (appendToBackstack) {
            true -> history.pushState(null, "", value)
            false -> history.replaceState(null, "", value)
        }
    }

    private fun routeOf(url: URL): AppRoute? {
        return AppRoute.routeOf(url.pathname, url.search.removePrefix("?"), screens)
    }

    private fun queryRoute(): AppRoute? {
        return document.queryAttribute(Attribute.RoutePath)?.let { routeOf(URL(it, window.location.href)) }
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

/** A query parameter of the current route, set to [value] under [key]. */
@JsPlainObject
external interface QueryParamChange : JsAny {
    val key: String
    val value: String
}

/** The event asking [Portal] to set a query parameter of the current route, dispatched on `window`. */
val QUERY_PARAM_CHANGE = EventType<CustomEvent<QueryParamChange>>("koala:query-param-change")
