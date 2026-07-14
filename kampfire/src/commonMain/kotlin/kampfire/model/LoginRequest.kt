package kampfire.model

import kampfire.api.LoginIdentity
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val loginIdentity: LoginIdentity,
    val isTemp: Boolean,
    val password: String? = null,
)