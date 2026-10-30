package streetlight.compose.model

import androidx.navigation3.runtime.NavKey
import kampfire.api.Slug
import koala.model.WebRoute
import kotlinx.serialization.Serializable
import streetlight.model.ui.StarDashTab
import streetlight.model.ui.EventRoute as WebEventRoute
import streetlight.model.ui.GalaxyFeedRoute as WebGalaxyFeedRoute
import streetlight.model.ui.GalaxyRoute as WebGalaxyRoute
import streetlight.model.ui.StarFeedRoute as WebStarFeedRoute
import streetlight.model.ui.LocationRoute as WebLocationRoute

/** A place in the app, held on the back stack of [Portal]. */
@Serializable
sealed interface AppRoute : NavKey

@Serializable
data object HomeRoute : AppRoute

@Serializable
data class GalaxyRoute(val slug: Slug) : AppRoute

@Serializable
data class EventRoute(val slug: Slug) : AppRoute

@Serializable
data class LocationRoute(val slug: Slug) : AppRoute

@Serializable
data object EarthRoute : AppRoute

/** The feed of every galaxy. */
@Serializable
data object GalaxyFeedRoute : AppRoute

/** The feed of every star. */
@Serializable
data object StarFeedRoute : AppRoute

@Serializable
data class GalaxyMapRoute(val slug: Slug) : AppRoute

/** The scouting of a new event, to be posted to the galaxy of [slug], when given. */
@Serializable
data class EventScoutRoute(val slug: Slug?) : AppRoute

/** The scouting of a new location, to be posted to the galaxy of [slug], when given. */
@Serializable
data class LocationScoutRoute(val slug: Slug?) : AppRoute

/** The forging of a new media post, to be posted to the galaxy of [slug], when given. */
@Serializable
data class MediaForgeRoute(val slug: Slug?) : AppRoute

/** The signed-in star's dashboard, showing its [tab], or the first. */
@Serializable
data class StarDashRoute(val tab: StarDashTab? = null) : AppRoute

/** The signed-in star's account settings. */
@Serializable
data object StarConfigRoute : AppRoute

@Serializable
data object AppConfigRoute : AppRoute

/** The app's route to the page of this web route, `null` for a page the app does not have. */
fun WebRoute.toAppRoute(): AppRoute? = when (this) {
    is WebGalaxyRoute -> GalaxyRoute(slug)
    is WebEventRoute -> EventRoute(slug)
    is WebLocationRoute -> LocationRoute(slug)
    is WebGalaxyFeedRoute -> GalaxyFeedRoute
    is WebStarFeedRoute -> StarFeedRoute
    else -> null
}
