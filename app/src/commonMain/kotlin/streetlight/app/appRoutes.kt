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