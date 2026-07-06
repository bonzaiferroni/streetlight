package koala.dom

import kampfire.model.Labeled
import koala.css.*
import koala.html.AppRoute
import koala.html.RouteMenu
import koala.html.filigree
import koala.html.navigation
import koala.html.row
import koala.html.textBlock
import koala.html.IconAction
import koala.html.IconButton
import koala.html.IconRoute

fun TagScope.routeMenu(
    context: String,
    routeNow: MenuItem,
    routes: List<MenuItem?>,
    mod: ModifierSet? = null,
    leftIcons: List<IconButton>? = null,
    rightIcons: List<IconButton>? = null,
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
                val item = item ?: return@forEach
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

internal fun TagScope.iconsTray(
    icons: List<IconButton>,
    mod: ModifierSet
) {
    row(modify(mod, BorderSolid2Px)) {
        icons.forEach { icon ->
            when (icon) {
                is IconAction -> icon(icon.svg, modify(Height3)).onClick(icon.action)
                is IconRoute -> navigation(icon.route, modify(Height3)) {
                    icon(icon.svg, modify(Height100P))
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