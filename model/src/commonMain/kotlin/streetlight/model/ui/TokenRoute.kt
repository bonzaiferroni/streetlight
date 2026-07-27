package streetlight.model.ui

import kampfire.api.ActionResult
import kampfire.model.Token
import streetlight.model.data.AuthTokenType

interface TokenRoute: StringIdRoute {
    val token: Token
    val tokenType: AuthTokenType
    override val id get() = token.value
}

data class VerifyEmailRoute(override val token: Token): TokenRoute {
    override val screen get() = Screen.VerifyEmail
    override val title get() = "Verify Email"
    override val tokenType get() = AuthTokenType.EmailVerification
}

data class AccountNotOwnedRoute(override val token: Token): TokenRoute {
    override val screen get() = Screen.AccountNotOwned
    override val title get() = "Account Not Owned"
    override val tokenType get() = AuthTokenType.AccountNotOwned
}

data class PasswordResetRoute(override val token: Token): TokenRoute {
    override val screen get() = Screen.PasswordReset
    override val title get() = "Password Reset"
    override val tokenType get() = AuthTokenType.PasswordReset
}

data class AccountLockdownRoute(override val token: Token): TokenRoute {
    override val screen get() = Screen.AccountLockdown
    override val title get() = "Account Lockdown"
    override val tokenType get() = AuthTokenType.AccountLockdown
}

data class ActionReportRoute(val result: ActionResult): IntIdRoute {
    override val screen get() = Screen.ActionReport
    override val title get() = "Result"
    override val id get() = result.ordinal
}