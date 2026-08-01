package kampfire.model

import kampfire.api.EmailAddress
import kampfire.api.Password
import kotlinx.serialization.Serializable

@Serializable
data class PasswordChange(
    val passwordNow: String?,
    val newPassword: String,
)

@Serializable
data class EmailChange(
    val passwordNow: String?,
    val newEmail: EmailAddress,
)

@Serializable
data class PasswordResetRequest(
    val token: Token,
    val password: String,
)