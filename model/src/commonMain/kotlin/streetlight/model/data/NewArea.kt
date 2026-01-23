package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewArea(
    val name: String,
    val areaType: AreaType
)