package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable

/** The few fields of a galaxy a list of lit galaxies needs. */
@Serializable
data class GalaxyLight(
    val slug: String,
    val name: String,
    val imageUrl: Url?,
)