package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: Long,
    val userId: Long,
    val name: String,
    val artist: String?,
    val music: String?,
)