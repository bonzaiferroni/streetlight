package streetlight.model.ui

import kampfire.api.Username

data class StarRoute(val username: Username): StreetlightRoute, SlugRoute {
    override val screen get() = Screen.Star
    override val title get() = "Star"
    override val slug get() = username
}

object StarDashRoute: StreetlightRoute {
    override val screen get() = Screen.StarDash
    override val title get() = "You"
}

object StarConfigRoute: StreetlightRoute {
    override val screen get() = Screen.StarConfig
    override val title get() = "Edit Profile"
}