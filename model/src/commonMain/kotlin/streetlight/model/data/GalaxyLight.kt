package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable

@Serializable
data class GalaxyLight(
    val slug: String,
    val name: String,
    val imageUrl: Url?,
)