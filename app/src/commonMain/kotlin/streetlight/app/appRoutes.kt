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
object AreaListRoute : AppRoute("Locations")

@Serializable
data class AreaProfileRoute(val areaId: Int) : AppRoute("Area Profile")

@Serializable
data class LocationProfileRoute(val locationId: Int) : AppRoute("Location Profile")

@Serializable
object SongListRoute : AppRoute("Songs")

@Serializable
data class SongProfileRoute(val songId: Long) : AppRoute("Song")