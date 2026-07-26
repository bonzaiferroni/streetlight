package streetlight.model.ui

import kampfire.api.Username
import kampfire.model.Token

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

data class VerifyEmailRoute(val token: Token): StreetlightRoute, StringIdRoute {
    override val screen get() = Screen.VerifyEmail
    override val title get() = "Verify Email"
    override val id get() = token.value
}

data class NotOwnedEmailRoute(val token: Token): StreetlightRoute, StringIdRoute {
    override val screen get() = Screen.DisavowEmail
    override val title get() = "Disavow Email"
    override val id get() = token.value
}

data class PasswordResetRoute(val token: Token): StreetlightRoute, StringIdRoute {
    override val screen get() = Screen.PasswordReset
    override val title get() = "Password Reset"
    override val id get() = token.value
}

data class AccountLockdownRoute(val token: Token): StreetlightRoute, StringIdRoute {
    override val screen get() = Screen.AccountLockdown
    override val title get() = "Account Lockdown"
    override val id get() = token.value
}