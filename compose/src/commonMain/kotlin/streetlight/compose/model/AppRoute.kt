package streetlight.compose.model

import androidx.navigation3.runtime.NavKey
import kampfire.api.Slug
import kotlinx.serialization.Serializable

/** A place in the app, held on the back stack of [Portal]. */
@Serializable
sealed interface AppRoute : NavKey

@Serializable
data object HomeRoute : AppRoute

@Serializable
data class GalaxyRoute(val slug: Slug) : AppRoute

@Serializable
data class EventRoute(val slug: Slug) : AppRoute
