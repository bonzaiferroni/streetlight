package streetlight.compose.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** A place in the app, held on the back stack of [Portal]. */
@Serializable
sealed interface AppRoute : NavKey

@Serializable
data object HomeRoute : AppRoute
