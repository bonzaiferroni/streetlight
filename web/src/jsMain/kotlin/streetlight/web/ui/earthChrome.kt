package streetlight.web.ui

import koala.SvgFile
import koala.css.AccentFg
import koala.css.AlignItemsStart
import koala.css.BlurBackdrop
import koala.css.Gap0
import koala.css.Gap2
import koala.css.JustifyContentEnd
import koala.css.JustifyContentSpaceBetween
import koala.css.Magic
import koala.css.OpacityHigh
import koala.css.OpacityLow
import koala.css.PointerEventsAuto
import koala.css.PrimaryFg
import koala.css.WidthFitContent
import koala.css.Zen
import koala.css.modify
import koala.dom.AppScope
import koala.dom.MenuLabel
import koala.dom.MenuRoute
import koala.dom.button
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.onClick
import koala.dom.routeMenu
import koala.dom.row
import koala.dom.textBlock
import koala.html.RouteMenuIcon
import koala.html.filigree
import koala.html.span
import streetlight.web.CityMap
import streetlight.web.CityMapRoute
import streetlight.web.CityRoute
import streetlight.web.EarthLayer
import streetlight.web.EarthMap
import streetlight.web.GalaxyMap
import streetlight.web.GalaxyMapRoute
import streetlight.web.GalaxyRoute
import streetlight.web.HomeRoute
import streetlight.web.model.Earth
import streetlight.web.model.MarkerType

fun AppScope.earthChrome(model: Earth) {
    column(modify(EarthStyle.Window, EarthStyle.MoveDimmer, JustifyContentSpaceBetween)) {
        row(modify(JustifyContentEnd, AlignItemsStart)) {
            flowBlock(model.summaryFlow) { summary ->
                if (summary.isNullOrEmpty()) return@flowBlock
                column(modify(WidthFitContent, Gap0)) {
                    filigree {
                        textBlock("In View", modify(OpacityHigh))
                    }
                    row(modify(Gap2)) {
                        summary.forEach { (markerType, count) ->
                            textBlock {
                                when (markerType) {
                                    MarkerType.Event -> span("Events", modify(AccentFg))
                                    MarkerType.Location -> span("Locations", modify(PrimaryFg))
                                    MarkerType.Galaxy -> span("Galaxies", modify())
                                    MarkerType.Media -> span("Media", modify())
                                    MarkerType.City -> span("Cities", modify())
                                }
                                span(" | ", modify(OpacityLow))
                                span(count.toString())
                            }
                        }
                    }
                }
            }
            button("Show All", modify(Zen, PointerEventsAuto, BlurBackdrop)).onClick(model::showAll)
        }
        flowBlock(model.mapFlow, modify(Magic)) { map ->
            earthRouteMenu(map)
        }
    }
}

fun AppScope.earthRouteMenu(map: EarthMap) = when (map) {
    is GalaxyMap -> {
        when (val galaxy = map.galaxy) {
            null -> {
                val routeNow = MenuLabel("Galaxies")
                routeMenu(
                    context = "Streetlight",
                    routeNow = routeNow,
                    routes = listOf(routeNow, MenuRoute(CityMapRoute(null), "Cities")),
                    mod = modify(PointerEventsAuto),
                    leftIcons = listOf(RouteMenuIcon(SvgFile.Home, HomeRoute))
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
                    leftIcons = listOf(RouteMenuIcon(SvgFile.CaretLeft, GalaxyMapRoute(null)))
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
                    leftIcons = listOf(RouteMenuIcon(SvgFile.Home, HomeRoute))
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
                    leftIcons = listOf(RouteMenuIcon(SvgFile.CaretLeft, CityMapRoute(null)))
                )
            }
        }
    }
}