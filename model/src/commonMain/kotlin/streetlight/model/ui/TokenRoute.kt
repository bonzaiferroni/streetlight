package streetlight.model.ui

import kampfire.api.ActionResult
import kampfire.model.Token

interface TokenRoute: StringIdRoute {
    val token: Token
    override val id get() = token.value
}

data class VerifyEmailRoute(override val token: Token): TokenRoute {
    override val screen get() = Screen.VerifyEmail
    override val title get() = "Verify Email"
}

data class AccountNotOwnedRoute(override val token: Token): TokenRoute {
    override val screen get() = Screen.AccountNotOwned
    override val title get() = "Account Not Owned"
}

data class PasswordResetRoute(override val token: Token): TokenRoute {
    override val screen get() = Screen.PasswordReset
    override val title get() = "Password Reset"
}

data class AccountLockdownRoute(override val token: Token): TokenRoute {
    override val screen get() = Screen.AccountLockdown
    override val title get() = "Account Lockdown"
}

data class ActionReportRoute(val result: ActionResult): IntIdRoute {
    override val screen get() = Screen.ActionReport
    override val title get() = "Result"
    override val id get() = result.ordinal
}