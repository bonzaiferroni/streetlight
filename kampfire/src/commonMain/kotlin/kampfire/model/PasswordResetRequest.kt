package kampfire.model

import kampfire.api.Email
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
    val newEmail: Email,
)

@Serializable
data class PasswordResetRequest(
    val token: Token,
    val password: Password,
)