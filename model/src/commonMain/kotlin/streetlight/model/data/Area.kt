package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Area(
    val id: Int,
    val name: String,
)