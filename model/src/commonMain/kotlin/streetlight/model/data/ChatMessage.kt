package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val source: String,
    val time: Long,
    val text: String,
)