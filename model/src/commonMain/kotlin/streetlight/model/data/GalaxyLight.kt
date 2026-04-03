package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class GalaxyLight(
    val path: String,
    val name: String,
    val imageUrl: String?,
)