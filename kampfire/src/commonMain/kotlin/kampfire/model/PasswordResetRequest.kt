package kampfire.model

import kampfire.api.Password
import kotlinx.serialization.Serializable

@Serializable
data class PasswordChange(
    val passwordNow: String?,
    val newPassword: String,
)

@Serializable
data class PasswordResetRequest(
    val token: Token,
    val password: Password,
)