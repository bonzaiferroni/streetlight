package streetlight.app

import pondui.ui.nav.NavRoute
import kotlinx.serialization.Serializable
import pondui.ui.nav.IdRoute
import pondui.ui.nav.matchStringIdRoute

@Serializable
sealed class AppRoute(
    override val title: String,
    val id: Long? = null
) : NavRoute {
    private val titlePath get() = title.lowercase().replace(' ', '-')

    override fun toPath() = id?.let { "$titlePath/$it" } ?: titlePath

    fun matchRoute(path: String) = if (path.split("/")[0] == titlePath) this else null
}

@Serializable
object StartRoute : AppRoute("Start")

@Serializable
object HelloRoute : AppRoute("Hello")

@Serializable
object EventFeedRoute : AppRoute("Events")

@Serializable
object StreetListRoute : AppRoute("Streets")

@Serializable
object SongFeedRoute : AppRoute("Songs")

@Serializable
data class StreetProfileRoute(override val id: String) : IdRoute<String> {
    override val title get() = TITLE
    companion object {
        const val TITLE = "Street"
        fun matchRoute(path: String) = matchStringIdRoute(path, TITLE) { StreetProfileRoute(it) }
    }
}

@Serializable
data class LocationProfileRoute(override val id: String) : IdRoute<String> {
    override val title get() = TITLE
    companion object {
        const val TITLE = "Location"
        fun matchRoute(path: String) = matchStringIdRoute(path, TITLE) { LocationProfileRoute(it) }
    }
}

@Serializable
data class SongProfileRoute(override val id: String) : IdRoute<String> {
    override val title get() = TITLE
    companion object {
        const val TITLE = "Song"
        fun matchRoute(path: String) = matchStringIdRoute(path, TITLE) { SongProfileRoute(it) }
    }
}

@Serializable
data class EventProfileRoute(override val id: String) : IdRoute<String> {
    override val title get() = TITLE
    companion object {
        const val TITLE = "Event"
        fun matchRoute(path: String) = matchStringIdRoute(path, TITLE) { EventProfileRoute(it) }
    }
}