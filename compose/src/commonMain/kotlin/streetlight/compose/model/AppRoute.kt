package streetlight.compose.model

import androidx.navigation3.runtime.NavKey
import kampfire.api.Slug
import koala.model.WebRoute
import kotlinx.serialization.Serializable
import streetlight.model.ui.EventRoute as WebEventRoute
import streetlight.model.ui.GalaxyRoute as WebGalaxyRoute
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

/** The app's route to the page of this web route, `null` for a page the app does not have. */
fun WebRoute.toAppRoute(): AppRoute? = when (this) {
    is WebGalaxyRoute -> GalaxyRoute(slug)
    is WebEventRoute -> EventRoute(slug)
    is WebLocationRoute -> LocationRoute(slug)
    else -> null
}
