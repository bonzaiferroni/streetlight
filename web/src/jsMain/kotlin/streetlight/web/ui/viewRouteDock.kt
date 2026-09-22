package streetlight.web.ui

import koala.SvgFile
import koala.dom.*
import koala.html.AppRoute
import koala.html.RouteMenu
import koala.html.navigation
import koala.html.row
import koala.html.textBlock
import koala.modifier.*
import koala.modifier.Height
import kotlinx.css.pct
import streetlight.model.ui.EarthRoute
import streetlight.web.pages.AppOverlay

fun ViewScope.wireRouteDock() {
    mountChildView(AppOverlay.RouteDockId) {
        viewRouteDock()
    }
}

fun ViewScope.viewRouteDock() {
    val model = dock

    column(modify(TextUppercase, TextSmall, Gap(0), AlignItemsCenter)) {
        flowBlock(model.titleState, modify(Magic, Height(3))) { title ->
            if (title == null) return@flowBlock
            filigree(AlignSelfStretch) {
                textBlock(title)
            }
        }
        row {
            val trayMod = modify()
            flowBlock(model.leftRoutes, modify(Flex1, Magic)) { leftRoutes ->
                val routes = leftRoutes?.takeIf { it.isNotEmpty() } ?: return@flowBlock
                row(trayMod) {
                    routes.forEach { route ->
                        navigation(route, SmallIconHeight) {
                            icon(iconOf(route), Height(100.pct))
                        }
                    }
                }
            }
            flowBlock(model.mainRoutes, Magic) { mainRoutes ->
                val routes = mainRoutes ?: return@flowBlock
                row(modify(Gap(0), Padding(1), Bold, BorderSolid2Px)) {
                    routes.forEach { route ->
                        navigation(route, RouteMenu.Route) { +route.label }
                    }
                }
            }
            flowBlock(model.rightRoutes, modify(Flex1, Magic)) { rightRoutes ->
                // similar to left routes
            }
        }
    }
}

private fun iconOf(route: AppRoute) = when(route) {
    is EarthRoute -> SvgFile.Earth
    else -> error("")
}