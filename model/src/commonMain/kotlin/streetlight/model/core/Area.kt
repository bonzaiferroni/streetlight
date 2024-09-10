package streetlight.model.core

import kotlinx.serialization.Serializable

@Serializable
data class Area(
    override val id: Int = 0,
    val name: String = "",
    val parentId: Int? = null,
) : IdModel