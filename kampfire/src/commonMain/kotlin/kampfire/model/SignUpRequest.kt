package kampfire.model

import kampfire.api.EmailAddress
import kampfire.api.Username
import kotlinx.serialization.Serializable

/** A request to create an account. A guest account may have no password or email. */
@Serializable
data class SignUpRequest(
    val username: Username,
    val password: String?,
    val email: EmailAddress?,
    val accountType: AccountType,
    val stayLoggedIn: Boolean,
)

/** A request to turn a guest account into a full one. */
@Serializable
data class AccountUpgradeRequest(
    val password: String,
    val email: EmailAddress?,
)