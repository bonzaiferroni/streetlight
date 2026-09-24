package streetlight.model.data

import kotlin.time.Instant
import kotlinx.serialization.Serializable

/** A message in a group chat. */
@Serializable
data class ChatMessage(
    val source: String,
    val text: String,
    val sentAt: Instant,
)