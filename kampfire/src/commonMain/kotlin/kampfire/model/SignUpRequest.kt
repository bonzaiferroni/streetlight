package kampfire.model

import kampfire.api.Username
import kampfire.utils.validEmail
import kampfire.utils.validPassword
import kampfire.utils.validUsername
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val username: Username = Username.Empty,
    val password: String = "",
    val email: String? = null,
    val accountType: AccountType = AccountType.Registered,
    val stayLoggedIn: Boolean = true,
) {
    val isValid get() = password.validPassword && username.validUsername && email.validEmail
}