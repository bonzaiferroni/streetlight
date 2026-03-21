package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class GalaxyStar(
    val pathId: PathId,
    val name: String,
    val imageUrl: String?,
)