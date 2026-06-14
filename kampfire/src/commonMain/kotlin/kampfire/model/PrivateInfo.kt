package kampfire.model

import kampfire.api.Username
import kotlinx.serialization.Serializable

@Serializable
data class PrivateInfo(
    val name: String? = null,
    val email: String? = null,
)