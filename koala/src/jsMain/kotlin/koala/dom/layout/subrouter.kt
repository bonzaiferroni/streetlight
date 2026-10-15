package koala.dom.layout

import kampfire.model.Tap
import kampfire.model.tapOf
import koala.dom.ViewScope
import koala.dom.asWeb
import koala.dom.component.navigation
import koala.dom.component.span
import koala.layout.SubrouterStyle
import koala.model.Portal
import koala.model.SubRoute
import koala.model.lineage
import koala.modifier.*
import kotlinx.html.js.div
import kotlinx.html.js.h4
import web.html.HTMLDivElement

/**
 * The routes of type [R] that one screen shows: breadcrumbs from [label], which leads to [index], down to the
 * current route; links to the [menu] routes in a panel beside the content when the screen is wide; and the
 * [content] of the current route. A route of another type shows [index].
 *
 * [crumbLabel] names a route in the breadcrumbs and the menu, its title by default.
 */
inline fun <reified R: SubRoute<R>> ViewScope.subrouter(
    label: String,
    index: R,
    menu: List<R>,
    mod: Modifier? = null,
    noinline crumbLabel: (R) -> String = { it.title },
    noinline content: ViewScope.(R) -> Unit,
): HTMLDivElement {
    val routeState = app.get<Portal>().routeState.tapOf { it as? R ?: index }
    return subrouter(label, index, menu, routeState, mod, crumbLabel, content)
}

/** A [subrouter] showing each route of [routeState]. */
fun <R: SubRoute<R>> ViewScope.subrouter(
    label: String,
    index: R,
    menu: List<R>,
    routeState: Tap<R>,
    mod: Modifier? = null,
    crumbLabel: (R) -> String = { it.title },
    content: ViewScope.(R) -> Unit,
): HTMLDivElement = div {
    addModifiers(modify(SubrouterStyle.Class, mod))

    flowBlock(routeState, SubrouterStyle.Header) { route ->
        val crumbs = route.lineage().drop(1)
        h4 {
            addModifiers(SubrouterStyle.Crumbs)
            navigation(index) { +label }
            when (route) {
                index -> crumb(crumbLabel(index))
                else -> crumbs.forEach { crumbRoute ->
                    when (crumbRoute) {
                        route -> crumb(crumbLabel(crumbRoute))
                        else -> crumb(crumbLabel(crumbRoute), crumbRoute)
                    }
                }
            }
        }
    }

    flowBlock(routeState, SubrouterStyle.Panel) { route ->
        menu.forEach { menuRoute ->
            navigation(menuRoute) {
                if (menuRoute in route.lineage()) attributes["aria-current"] = "page"
                +crumbLabel(menuRoute)
            }
        }
    }

    flowBlock(routeState, SubrouterStyle.Content) { route -> content(route) }
}.asWeb()

/** A breadcrumb after a separator: [text], linking to [route] when given. */
private fun ViewScope.crumb(text: String, route: SubRoute<*>? = null) {
    span("›", OpacityHalf)
    when (route) {
        null -> span(text)
        else -> navigation(route) { +text }
    }
}
