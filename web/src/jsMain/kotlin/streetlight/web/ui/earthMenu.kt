package streetlight.web.ui

import koala.SvgFile
import koala.css.Magic
import koala.css.PointerEventsAuto
import koala.css.modify
import koala.dom.AppScope
import koala.dom.MenuButton
import koala.dom.MenuLabel
import koala.dom.MenuRoute
import koala.dom.flowBlock
import koala.dom.routeMenu
import koala.html.IconAction
import koala.html.IconRoute
import streetlight.web.CityMap
import streetlight.web.CityMapRoute
import streetlight.web.CityRoute
import streetlight.web.EarthMap
import streetlight.web.GalaxyMap
import streetlight.web.GalaxyMapRoute
import streetlight.web.GalaxyRoute
import streetlight.web.HomeRoute
import streetlight.web.model.Earth

fun AppScope.earthMenu(model: Earth) {
    flowBlock(model.mapFlow, modify(Magic, EarthStyle.Window, EarthStyle.MoveDimmer)) { map ->
        earthRouteMenu(model, map)
    }
}

fun AppScope.earthRouteMenu(model: Earth, map: EarthMap) {
    val showAll = IconAction(SvgFile.FrameEye, model::showAll)
    when (map) {
        is GalaxyMap -> {
            when (val galaxy = map.galaxy) {
                null -> {
                    val routeNow = MenuLabel("Galaxies")
                    routeMenu(
                        context = "Streetlight",
                        routeNow = routeNow,
                        routes = listOf(routeNow, MenuRoute(CityMapRoute(null), "Cities")),
                        mod = modify(PointerEventsAuto),
                        leftIcons = listOf(IconRoute(SvgFile.Home, HomeRoute)),
                        rightIcons = listOf(showAll)
                    )
                }
                else -> {
                    val routeNow = MenuLabel("Map")
                    routeMenu(
                        context = galaxy.name,
                        routeNow = routeNow,
                        routes = listOf(
                            MenuRoute(GalaxyRoute(galaxy.slug), "Feed"),
                            routeNow
                        ),
                        mod = modify(PointerEventsAuto),
                        leftIcons = listOf(IconRoute(SvgFile.CaretLeft, GalaxyMapRoute(null))),
                        rightIcons = listOf(showAll)
                    )
                }
            }
        }
        is CityMap -> {
            when (val city = map.city) {
                null -> {
                    val routeNow = MenuLabel("Cities")
                    routeMenu(
                        context = "Streetlight",
                        routeNow = routeNow,
                        routes = listOf(MenuRoute(GalaxyMapRoute(null), "Galaxies"), routeNow),
                        mod = modify(PointerEventsAuto),
                        leftIcons = listOf(IconRoute(SvgFile.Home, HomeRoute)),
                        rightIcons = listOf(showAll)
                    )
                }
                else -> {
                    val routeNow = MenuLabel("Map")
                    routeMenu(
                        context = city.name,
                        routeNow = routeNow,
                        routes = listOf(
                            MenuRoute(CityRoute(city.slug), "Feed"),
                            routeNow,
                        ),
                        mod = modify(PointerEventsAuto),
                        leftIcons = listOf(IconRoute(SvgFile.CaretLeft, CityMapRoute(null))),
                        rightIcons = listOf(showAll)
                    )
                }
            }
        }
    }
}