package streetlight.compose.model

import kampfire.api.Slug
import koala.SvgFile
import koala.SvgPack

/** A route the dock opens, shown as [label]. */
data class DockRoute(val label: String, val route: AppRoute)

/** The routes of the dock shown on every page, the first the start of the [Portal]. */
val StableDock = listOf(
    DockRoute("Home", HomeRoute),
    DockRoute("Earth", EarthRoute),
    DockRoute("Galaxies", GalaxyFeedRoute),
    DockRoute("Stars", StarFeedRoute),
)

/** The dock of the content a route focuses on: its [routes], and the [icon] that calls it up. */
data class FocusDock(val routes: List<DockRoute>, val icon: SvgPack)

/** The dock of the content this route focuses on, `null` for a route with no focus. */
fun AppRoute.toFocusDock(): FocusDock? = when (this) {
    is GalaxyRoute -> galaxyDockOf(slug)
    is GalaxyMapRoute -> galaxyDockOf(slug)
    else -> null
}

private fun galaxyDockOf(slug: Slug) = FocusDock(
    routes = listOf(
        DockRoute("Feed", GalaxyRoute(slug)),
        DockRoute("Map", GalaxyMapRoute(slug)),
    ),
    icon = SvgFile.Planet,
)
