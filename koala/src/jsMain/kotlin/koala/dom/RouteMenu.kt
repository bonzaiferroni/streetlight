package koala.dom

import kampfire.model.Labeled
import koala.SvgFile
import koala.css.*
import koala.html.AppRoute
import koala.html.RouteMenu
import koala.html.RouteMenuIcon
import koala.html.filigree
import koala.html.icon
import koala.html.iconsTray
import koala.html.navigation
import koala.html.row
import koala.html.textBlock

fun TagScope.routeMenu(
    context: String,
    routeNow: MenuItem,
    routes: List<MenuItem>,
    mod: ModifierSet? = null,
    leftIcons: List<RouteMenuIcon>? = null,
    rightIcons: List<RouteMenuIcon>? = null,
) {
    column(modify(mod, RouteMenu.Base, TextTransformUppercase, TextSmall, Gap0, AlignItemsCenter)) {
        filigree(modify(AlignSelfStretch)) {
            textBlock(context)
        }
        row(modify(RouteMenu.ContextMenu, Gap0, TextTransformUppercase, TextSmall, Padding1, Bold, BorderSolid2Px)) {
            leftIcons?.let { icons ->
                iconsTray(icons, modify(RouteMenu.LeftTray))
            }
            routes.forEach { item ->
                when (item.label == routeNow.label) {
                    true -> span(item.label, modify(RouteMenu.RouteNow))
                    else -> routeMenuItem(item)
                }
            }
            rightIcons?.let { icons ->
                iconsTray(icons, modify(RouteMenu.RightTray))
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