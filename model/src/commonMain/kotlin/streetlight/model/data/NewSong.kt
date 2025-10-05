package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class NewSong(
    val title: String,
    val artist: String,
)