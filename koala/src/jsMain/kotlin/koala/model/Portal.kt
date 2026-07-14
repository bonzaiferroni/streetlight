package koala.model

import koala.css.KoalaBody
import koala.dom.setAttribute
import koala.html.AppRoute
import koala.html.AppScreen
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.MANUAL
import org.w3c.dom.ScrollRestoration
import org.w3c.dom.Window
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.url.URL
import kotlin.time.Clock
import kotlin.time.Instant

class Portal(
    initialRoute: AppRoute,
    val screens: List<AppScreen>,
    private val scope: CoroutineScope
) {
    private val state = storeOf(PortalState(routeOf(window.location.pathname) ?: initialRoute))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val screenFlow = stateFlow.tap { it.route.screen }
    val routeFlow = stateFlow.tap { it.route }

    private var backstack: List<Navigation> = emptyList()

    var sitePath
        get() = window.location.pathname
        set(value: String) {
            if (window.location.pathname == value) return
            val href = "${window.location.origin}$value${window.location.search}"
            window.history.pushState(null, "", href)
        }

    init {
        // hack to keep scroll from jumping on back press
        // known issue: this prevents scroll restoration on refresh
        window.history.scrollRestoration = ScrollRestoration.MANUAL

        fun handleRoute(href: String) {
            val href = window.prefixContext(href)
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
            val isSuccess = goBack(window.location.pathname)
            if (!isSuccess) {
                handleRoute(window.location.pathname)
            }
        })
    }

    inline fun <reified T : AppRoute> routeFlowOf(emitDistinct: Boolean = true): Flow<T> {
        val base = stateFlow.map {
            try {
                it.route as? T
            } catch (e: Exception) {
                console.log("unable to cast route: ${e.message}")
                throw (e)
            }
        }.filterNotNull()

        return if (emitDistinct) base.distinctUntilChanged() else base
    }

    fun go(route: AppRoute) {
        val navigation = Navigation(route, 0.0)
        go(navigation, backstack + Navigation(stateNow.route, window.scrollY))
    }

    fun goBack(sitePath: String? = null): Boolean {
        val (index, route) = sitePath?.let {
            backstack.asReversed()
                .withIndex()
                .firstOrNull { (_, value) -> value.route.toSitePath() == it }
        } ?: backstack.lastOrNull()?.let { IndexedValue(0, it) } ?: return false
        go(route, backstack.dropLast(index + 1))
        return true
    }

    fun refresh() {
        state.set { it.copy(refreshedAt = Clock.System.now(), isInitialRoute = false) }
    }

    private fun go(navigation: Navigation, backstack: List<Navigation>) {
        val route = navigation.route
        this.backstack = backstack
        state.set { it.copy(
            route = route,
            title = route.title,
            canGoBack = backstack.isNotEmpty(),
            initialScrollY = navigation.initialScrollY,
            isInitialRoute = false,
            refreshedAt = Clock.System.now()
        )}
        sitePath = route.toSitePath()

        document.body?.setAttribute(KoalaBody.ScreenId.to(route.screen.screenId))
    }

    private fun routeOf(hashPath: String): AppRoute? {
        return AppRoute.routeOf(hashPath, screens)
    }
}

data class PortalState(
    val route: AppRoute,
    val title: String? = null,
    val canGoBack: Boolean = false,
    val initialScrollY: Double = window.scrollY,
    val isInitialRoute: Boolean = true,
    val refreshedAt: Instant = Instant.DISTANT_PAST,
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