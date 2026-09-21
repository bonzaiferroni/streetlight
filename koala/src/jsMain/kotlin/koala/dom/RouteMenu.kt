package koala.dom

import koala.modifier.*
import koala.html.AppRoute
import koala.html.RouteMenu
import koala.html.filigree
import koala.html.navigation
import koala.html.row
import koala.html.textBlock
import koala.html.IconAction
import koala.html.IconButton
import koala.html.IconRoute
import kotlinx.css.pct

fun ViewScope.routeMenu(
    context: String,
    optionNow: MenuOption,
    options: List<MenuOption?>,
    mod: Modifier? = null,
    leftIcons: List<IconButton>? = null,
    rightIcons: List<IconButton>? = null,
) {
    column(modify(mod, RouteMenu.Base, TextUppercase, TextSmall, Gap(0), AlignItemsCenter)) {
        filigree(modify(AlignSelfStretch)) {
            textBlock(context)
        }
        row(modify(RouteMenu.ContextMenu, Gap(0), TextUppercase, TextSmall, Padding(1), Bold, BorderSolid2Px)) {
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
    mod: Modifier
) {
    row(modify(mod, BorderSolid2Px)) {
        icons.forEach { icon ->
            when (icon) {
                is IconAction -> icon(icon.svg, modify(SmallIconHeight)).onClick(icon.action)
                is IconRoute -> navigation(icon.route, modify(SmallIconHeight)) {
                    icon(icon.svg, modify(Height(100.pct)))
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