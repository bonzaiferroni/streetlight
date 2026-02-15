package streetlight.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val source: String,
    val text: String,
    val sentAt: Instant,
)