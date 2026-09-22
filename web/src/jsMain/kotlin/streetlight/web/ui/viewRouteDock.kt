package streetlight.web.ui

import koala.SvgFile
import koala.dom.*
import koala.html.AppRoute
import koala.html.navigation
import koala.html.row
import koala.html.textBlock
import koala.modifier.*
import koala.modifier.Height
import kotlinx.css.pct
import streetlight.model.ui.EarthRoute
import streetlight.model.ui.GalaxyConfigRoute
import streetlight.model.ui.HomeRoute
import streetlight.model.ui.LocationConfigRoute
import streetlight.model.ui.ProfileConfigRoute
import streetlight.model.ui.StarConfigRoute
import streetlight.web.pages.AppOverlay

fun ViewScope.wireRouteDock() {
    mountChildView(AppOverlay.RouteDockId) {
        viewRouteDock()
    }
}

fun ViewScope.viewRouteDock() {
    val model = dock

    column(modify(PointerEventsAuto, TextUppercase, TextSmall, Gap(0), MarginBottom(2))) {
        flowBlock(model.titleState, modify(Magic, Height(3))) { title ->
            if (title == null) return@flowBlock
            filigree(AlignSelfStretch) {
                textBlock(title, PrimaryFg)
            }
        }
        row(modify(Gap(1), AlignItemsCenter)) {
            val trayMod = modify(RouteDockStyle.Glass, BlurBackdrop, BorderRadiusPill, BorderSolid2Px, Padding(1))
            flowBlock(model.leftRoutes, modify(Flex1, Magic, DisplayFlex, JustifyContentEnd)) { leftRoutes ->
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
                row(modify(RouteDockStyle.Glass, BlurBackdrop, BorderRadiusPill, Gap(0), Padding(1), Bold, AlignItemsCenter, BorderSolid2Px)) {
                    routes.forEach { route ->
                        navigation(route, modify(BorderRadiusPill, PaddingY1, PaddingX2)) { +route.label }
                    }
                }
            }
            flowBlock(model.rightRoutes, modify(Flex1, Magic, DisplayFlex, JustifyContentStart)) { rightRoutes ->
                val routes = rightRoutes?.takeIf { it.isNotEmpty() } ?: return@flowBlock
                row(trayMod) {
                    routes.forEach { route ->
                        navigation(route, SmallIconHeight) {
                            icon(iconOf(route), Height(100.pct))
                        }
                    }
                }
            }
        }
    }
}

private fun iconOf(route: AppRoute) = when(route) {
    is EarthRoute -> SvgFile.Earth
    is HomeRoute -> SvgFile.Home
    is GalaxyConfigRoute, is LocationConfigRoute, is ProfileConfigRoute, is StarConfigRoute -> SvgFile.GearSmall
    else -> error("no dock icon for route: $route")
}