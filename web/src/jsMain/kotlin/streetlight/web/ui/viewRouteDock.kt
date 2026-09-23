package streetlight.web.ui

import koala.SvgFile
import koala.dom.*
import koala.html.AppRoute
import koala.html.navigation
import koala.html.textBlock
import koala.modifier.*
import koala.modifier.Height
import kampfire.model.tapOf
import kotlinx.css.pct
import streetlight.model.ui.CityConfigRoute
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
    fun isRouteNow(route: AppRoute) = portal.routeState.tapOf { it == route }

    column(modify(RouteDockStyle.Container, PointerEventsAuto, TextUppercase, TextSmall, Gap(0), MarginBottom(2))) {
        flowBlock(model.titleState, modify(Magic, Height(3))) { title ->
            if (title == null) return@flowBlock
            filigree(ruleMod = MaxWidth(8)) {
                textBlock(title, PrimaryFg)
            }
        }
        row(modify(Gap(1), AlignItemsCenter)) {
            val trayMod = modify(RouteDockStyle.Glass, BlurBackdrop, BorderRadiusPill, BorderSolid2Px, Padding(1))
            flowBlock(model.leftRoutes, modify(Flex1, Magic, DisplayFlex, JustifyContentEnd)) { leftRoutes ->
                val routes = leftRoutes?.takeIf { it.isNotEmpty() } ?: return@flowBlock
                row(trayMod) {
                    routes.forEach { route ->
                        this@flowBlock.navigation(route, modify(SmallIconHeight, BorderRadiusPill)) {
                            icon(iconOf(route), Height(100.pct))
                        }.asWeb().flowModifier(isRouteNow(route), RouteDockStyle.RouteNow, contentScope)
                    }
                }
            }
            flowBlock(model.mainRoutes, Magic) { mainRoutes ->
                val routes = mainRoutes ?: return@flowBlock
                row(modify(RouteDockStyle.Glass, BlurBackdrop, BorderRadiusPill, Gap(0), Padding(1), Bold, AlignItemsCenter, BorderSolid2Px)) {
                    routes.forEach { route ->
                        this@flowBlock.navigation(route, modify(BorderRadiusPill, PaddingY1, PaddingX2)) { +route.label }
                            .asWeb().flowModifier(isRouteNow(route), RouteDockStyle.RouteNow, contentScope)
                    }
                }
            }
            flowBlock(model.rightRoutes, modify(Flex1, Magic, DisplayFlex, JustifyContentStart)) { rightRoutes ->
                val routes = rightRoutes?.takeIf { it.isNotEmpty() } ?: return@flowBlock
                row(trayMod) {
                    routes.forEach { route ->
                        this@flowBlock.navigation(route, modify(SmallIconHeight, BorderRadiusPill)) {
                            icon(iconOf(route), Height(100.pct))
                        }.asWeb().flowModifier(isRouteNow(route), RouteDockStyle.RouteNow, contentScope)
                    }
                }
            }
        }
    }.flowModifier(model.isVisibleState.tapOf { !it }, RouteDockStyle.Hidden, contentScope)
}

private fun iconOf(route: AppRoute) = when(route) {
    is EarthRoute -> SvgFile.Earth
    is HomeRoute -> SvgFile.Home
    is CityConfigRoute, is GalaxyConfigRoute, is LocationConfigRoute, is ProfileConfigRoute, is StarConfigRoute -> SvgFile.GearSmall
    else -> error("no dock icon for route: $route")
}