package kampfire.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val usernameOrEmail: String,
    val isTemp: Boolean,
    val password: String? = null,
)