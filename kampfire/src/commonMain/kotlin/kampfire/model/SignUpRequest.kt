package kampfire.model

import kampfire.api.Email
import kampfire.api.Password
import kampfire.api.Username
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val username: Username,
    val password: Password,
    val email: Email?,
    val accountType: AccountType,
    val stayLoggedIn: Boolean,
)