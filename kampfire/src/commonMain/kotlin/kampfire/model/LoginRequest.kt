package kampfire.model

import kampfire.api.LoginIdentity
import kotlinx.serialization.Serializable

/**
 * A request to sign in with a username or email address.
 *
 * A request with [isTemp] set keeps the session for the browser session only.
 */
@Serializable
data class LoginRequest(
    val loginIdentity: String,
    val isTemp: Boolean,
    val password: String? = null,
)