package streetlight.model.data

import kampfire.api.Slug
import kotlinx.serialization.Serializable

@Serializable
data class SubdomainConfig(
    val locationId: LocationId,
    val slug: Slug?
)