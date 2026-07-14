package streetlight.model.data

import kampfire.api.Email
import kotlinx.serialization.Serializable

@Serializable
data class IdentityInfo(
    val identityVisibility: IdentityVisibility,
    val name: String?,
    val email: Email?,
    val cityId: CityId?,
)