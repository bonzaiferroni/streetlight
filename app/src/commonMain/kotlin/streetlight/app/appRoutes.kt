package streetlight.app

import pondui.ui.nav.NavRoute
import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute(
    override val title: String,
) : NavRoute

@Serializable
object StartRoute : AppRoute("Start")

@Serializable
object HelloRoute : AppRoute("Hello")

@Serializable
object EventFeedRoute : AppRoute("Events")

@Serializable
object AreaListRoute : AppRoute("Areas")

@Serializable
data class LocationListRoute(val areaId: Int) : AppRoute("Locations")