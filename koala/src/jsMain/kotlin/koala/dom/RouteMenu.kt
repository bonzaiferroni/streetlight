package koala.dom

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

fun ViewScope.routeMenu(
    context: String,
    optionNow: MenuOption,
    options: List<MenuOption?>,
    mod: ModifierSet? = null,
    leftIcons: List<IconButton>? = null,
    rightIcons: List<IconButton>? = null,
) {
    column(modify(mod, RouteMenu.Base, TextUppercase, TextSmall, Gap0, AlignItemsCenter)) {
        filigree(modify(AlignSelfStretch)) {
            textBlock(context)
        }
        row(modify(RouteMenu.ContextMenu, Gap0, TextUppercase, TextSmall, Padding1, Bold, BorderSolid2Px)) {
            leftIcons?.let { icons ->
                iconsTray(icons, modify(RouteMenu.LeftTray))
            }
            options.forEach { item ->
                val item = item ?: return@forEach
                when (item.label == optionNow.label) {
                    true -> span(item.label, modify(RouteMenu.Route, RouteMenu.RouteNow))
                    else -> routeMenuItem(item)
                }
            }
            rightIcons?.let { icons ->
                iconsTray(icons, modify(RouteMenu.RightTray))
            }
        }
    }
}

internal fun ViewScope.iconsTray(
    icons: List<IconButton>,
    mod: ModifierSet
) {
    row(modify(mod, BorderSolid2Px)) {
        icons.forEach { icon ->
            when (icon) {
                is IconAction -> icon(icon.svg, modify(Height3)).onClick(icon.action)
                is IconRoute -> navigation(icon.route, modify(Height3)) {
                    icon(icon.svg, modify(Height100Pct))
                }
            }
        }
    }
}

fun ViewScope.routeMenuItem(item: MenuOption) = when (item) {
    is MenuAction -> span(item.label, modify(RouteMenu.Route)).onClick(item.onClick)
    is MenuRoute -> navigation(item.route, modify(RouteMenu.Route)) { +item.label }
    is MenuLabel -> span(item.label, modify(RouteMenu.Route))
}

fun AppRoute.toMenuRoute() = MenuRoute(this)