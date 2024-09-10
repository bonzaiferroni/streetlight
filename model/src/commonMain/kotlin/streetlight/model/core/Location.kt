package streetlight.model.core

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    override val id: Int = 0,
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val areaId: Int = 0,
) : IdModel