package kampfire.model

import kampfire.api.Email
import kampfire.api.Username
import kotlinx.serialization.Serializable

@Serializable
data class PrivateInfo(
    val name: String? = null,
    val email: Email? = null,
)