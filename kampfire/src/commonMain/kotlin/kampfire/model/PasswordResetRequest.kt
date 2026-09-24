package kampfire.model

import kampfire.api.EmailAddress
import kampfire.api.Password
import kotlinx.serialization.Serializable

/** A request to change the password, confirmed by the current one. */
@Serializable
data class PasswordChange(
    val passwordNow: String?,
    val newPassword: String,
)

/** The current password, sent to confirm an action on the account. */
@Serializable
data class PasswordVerification(
    val passwordNow: String?,
)

/** A request to change the email address, confirmed by the current password. */
@Serializable
data class EmailChange(
    val passwordNow: String?,
    val newEmail: EmailAddress,
)

/** A new password, set with the [token] from a reset email. */
@Serializable
data class PasswordResetRequest(
    val token: Token,
    val password: String,
)