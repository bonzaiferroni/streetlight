package streetlight.app

import pondui.ui.nav.NavRoute
import kotlinx.serialization.Serializable
import pondui.ui.nav.IdRoute
import streetlight.model.data.AreaId
import streetlight.model.data.toProjectId

@Serializable
sealed class AppRoute(
    override val title: String,
    val id: Long? = null
) : NavRoute {
    private val titlePath get() = title.lowercase().replace(' ', '-')

    override fun toPath() = id?.let { "$titlePath/$it" } ?: titlePath

    fun matchRoute(path: String) = if (path.startsWith(titlePath)) this else null
}

@Serializable
object StartRoute : AppRoute("Start")

@Serializable
object HelloRoute : AppRoute("Hello")

@Serializable
object EventFeedRoute : AppRoute("Events")

@Serializable
object AreaListRoute : AppRoute("Locations")

@Serializable
data class AreaProfileRoute(val areaId: String) : IdRoute<String>("Area Profile", areaId)

@Serializable
data class LocationProfileRoute(val locationId: String) : IdRoute<String>("Location Profile", locationId)

@Serializable
object SongListRoute : AppRoute("Songs")

@Serializable
data class SongProfileRoute(val songId: String) : IdRoute<String>("Song", songId)