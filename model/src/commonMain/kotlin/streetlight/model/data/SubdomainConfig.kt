package streetlight.model.data

import kampfire.api.Slug
import kotlinx.serialization.Serializable

/** The subdomain a location's page is served at. */
@Serializable
data class SubdomainConfig(
    val locationId: LocationId,
    val slug: Slug?
)