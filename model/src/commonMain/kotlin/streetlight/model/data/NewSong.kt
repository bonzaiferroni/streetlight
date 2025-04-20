package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewSong(
    val name: String,
    val artist: String?,
)