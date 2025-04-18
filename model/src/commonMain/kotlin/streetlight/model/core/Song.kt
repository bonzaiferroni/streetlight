package streetlight.model.core

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    override val id: Int = 0,
    val userId: Long = 0,
    val name: String = "",
    val artist: String? = null,
    val music: String? = null,
) : IdModel