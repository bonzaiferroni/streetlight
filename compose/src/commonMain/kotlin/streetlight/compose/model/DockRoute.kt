package streetlight.compose.model

/** A route the dock opens, shown as [label]. */
data class DockRoute(val label: String, val route: AppRoute)

/** The routes of the dock shown on every page, the first the start of the [Portal]. */
val StableDock = listOf(
    DockRoute("Home", HomeRoute),
    DockRoute("Earth", EarthRoute),
)
