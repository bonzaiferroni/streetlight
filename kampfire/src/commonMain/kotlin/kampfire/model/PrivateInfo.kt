package kampfire.model

import kampfire.api.EmailAddress
import kotlinx.serialization.Serializable

@Serializable
data class PrivateInfo(
    val name: String? = null,
    val email: EmailAddress? = null,
)