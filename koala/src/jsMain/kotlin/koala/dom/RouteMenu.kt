package koala.dom

import kampfire.model.Labeled
import koala.css.*
import koala.html.AppRoute
import koala.html.RouteMenu

fun TagScope.routeMenu(
    routeNow: MenuItem,
    routes: List<MenuItem>,
    mod: ModifierSet? = null,
) {
    row(modify(mod, RouteMenu.Mod, Gap0, TextTransformUppercase, TextSmall, Padding1, Bold)) {
        routes.forEach { item ->
            when (item.label == routeNow.label) {
                true -> span(item.label, modify(RouteMenu.RouteNow))
                else -> when (item) {
                    is MenuButton -> span(item.label, modify(RouteMenu.Route)).onClick(item.onClick)
                    is MenuRoute -> navigation(item.route, modify(RouteMenu.Route)) {
                        +item.label
                    }

                    is MenuLabel -> span(item.label, modify(RouteMenu.Route))
                }
            }
        }
    }
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

typealias MenuItemProto = Either<AppRoute, MenuButton>

sealed interface Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>
    data class Right<out R>(val value: R) : Either<Nothing, R>
}