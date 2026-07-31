package kampfire.model

import kampfire.api.EmailAddress
import kampfire.api.Username
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val username: Username,
    val password: String?,
    val email: EmailAddress?,
    val accountType: AccountType,
    val stayLoggedIn: Boolean,
)

@Serializable
data class AccountUpgradeRequest(
    val password: String,
    val email: EmailAddress?,
)