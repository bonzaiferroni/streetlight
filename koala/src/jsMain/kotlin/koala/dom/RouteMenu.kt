package koala.dom

import kampfire.model.Labeled
import koala.SvgFile
import koala.css.*
import koala.html.AppRoute
import koala.html.RouteMenu
import koala.html.filigree
import koala.html.icon
import koala.html.navigation
import koala.html.textBlock
import kotlinx.coroutines.flow.Flow

fun TagScope.routeMenu(
    context: String,
    routeNow: MenuItem,
    routes: List<MenuItem>,
    mod: ModifierSet? = null,
    backRoute: AppRoute? = null,
) {
    column(modify(mod, RouteMenu.Base, TextTransformUppercase, TextSmall, Gap0, AlignItemsCenter)) {
        filigree(modify(AlignSelfStretch)) {
            textBlock(context)
        }
        row(modify(RouteMenu.Menu, Gap0, TextTransformUppercase, TextSmall, Padding1, Bold)) {
            backRoute?.let {
                navigation(it, modify(RouteMenu.Back, Height4)) {
                    icon(SvgFile.ArrowLeft, modify(Width4, Height4))
                }
            }
            routes.forEach { item ->
                when (item.label == routeNow.label) {
                    true -> span(item.label, modify(RouteMenu.RouteNow))
                    else -> routeMenuItem(item)
                }
            }
        }
    }
}

fun TagScope.routeMenuItem(item: MenuItem) = when (item) {
    is MenuButton -> span(item.label, modify(RouteMenu.Route)).onClick(item.onClick)
    is MenuRoute -> navigation(item.route, modify(RouteMenu.Route)) { +item.label }
    is MenuLabel -> span(item.label, modify(RouteMenu.Route))
}

data class MenuRoute(
    val route: AppRoute,
    val customLabel: String? = null,
): MenuItem {
    override val label get() = customLabel ?: route.title
}

data class MenuButton(
    override val label: String,
    val onClick: () -> Unit
): MenuItem

data class MenuLabel(override val label: String): MenuItem

sealed interface MenuItem: Labeled