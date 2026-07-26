package kampfire.model

import kampfire.api.Password
import kotlinx.serialization.Serializable

@Serializable
data class PasswordChange(
    val currentPassword: Password,
    val newPassword: Password,
)

@Serializable
data class PasswordResetRedemption(
    val token: Token,
    val password: Password,
)