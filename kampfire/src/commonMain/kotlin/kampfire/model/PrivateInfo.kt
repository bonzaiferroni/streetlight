package kampfire.model

import kampfire.api.EmailAddress
import kotlinx.serialization.Serializable

/** The account details only its owner may read. */
@Serializable
data class PrivateInfo(
    val name: String? = null,
    val email: EmailAddress? = null,
)