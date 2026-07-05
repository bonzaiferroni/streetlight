package streetlight.web.ui

import koala.SvgFile
import koala.css.PointerEventsAuto
import koala.css.modify
import koala.dom.AppScope
import koala.dom.MenuLabel
import koala.dom.MenuRoute
import koala.dom.routeMenu
import koala.html.RouteMenuIcon
import streetlight.web.CityMap
import streetlight.web.CityMapRoute
import streetlight.web.EarthLayer
import streetlight.web.EarthMap
import streetlight.web.GalaxyMap
import streetlight.web.GalaxyMapRoute
import streetlight.web.GalaxyRoute
import streetlight.web.HomeRoute
import streetlight.web.model.Earth

fun AppScope.earthRouteMenu(model: Earth, map: EarthMap?) = when (map) {
    is GalaxyMap -> {
        val galaxy = map.galaxy
        val routeNow = MenuLabel("Map")
        routeMenu(
            context = galaxy.name,
            routeNow = routeNow,
            routes = listOf(
                MenuRoute(GalaxyRoute(galaxy.slug), "Feed"),
                routeNow
            ),
            mod = modify(PointerEventsAuto),
            leftIcons = listOf(RouteMenuIcon(SvgFile.CaretLeft, GalaxyMapRoute(null)))
        )
    }
    is CityMap -> {
        console.log("yer city map route menu") // ey
    }
    null -> {
        when (model.stateNow.layer) {
            EarthLayer.Galaxy -> {
                val routeNow = MenuLabel("Galaxies")
                routeMenu(
                    context = "Streetlight",
                    routeNow = routeNow,
                    routes = listOf(routeNow, MenuRoute(CityMapRoute(null), "Cities")),
                    mod = modify(PointerEventsAuto),
                    leftIcons = listOf(RouteMenuIcon(SvgFile.Home, HomeRoute))
                )
            }
            EarthLayer.City -> {
                val routeNow = MenuLabel("Cities")
                routeMenu(
                    context = "Streetlight",
                    routeNow = routeNow,
                    routes = listOf(MenuRoute(GalaxyMapRoute(null), "Galaxies"), routeNow),
                    mod = modify(PointerEventsAuto),
                    leftIcons = listOf(RouteMenuIcon(SvgFile.Home, HomeRoute))
                )
            }
        }
    }
}