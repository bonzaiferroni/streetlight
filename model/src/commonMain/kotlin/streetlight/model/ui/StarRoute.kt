package streetlight.model.ui

import kampfire.api.Username

data class StarRoute(val username: Username): StreetlightRoute, SlugRoute {
    override val screen get() = Screen.Star
    override val title get() = "Profile"
    override val slug get() = username
}

object StarDashRoute: StreetlightRoute {
    override val screen get() = Screen.StarDash
    override val title get() = "Messages"
}

object UpdateProfileRoute: StreetlightRoute {
    override val screen get() = Screen.UpdateProfile
    override val title get() = "Edit"
}

object UpdateAccountRoute: StreetlightRoute {
    override val screen get() = Screen.UpdateAccount
    override val title get() = "Account"
}