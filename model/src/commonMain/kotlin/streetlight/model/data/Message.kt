package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Message(
    val messageId: MessageId,
    val author: Username,
    val content: Markdown,
    val sentAt: Instant,
)

@Serializable
@JvmInline
value class MessageId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}

@Serializable
data class NewMessage(
    val recipient: Username,
    val subject: String?,
    val content: Markdown,
) {
    val isValid get() = content.value.isNotBlank()
}

@Serializable
data class ReplyMessage(
    val chatId: ChatId,
    val content: Markdown,
)

@Serializable
data class ChatPreview(
    val chatId: ChatId,
    val usernames: List<Username>,
    val subject: String?,
    val lastMessagePreview: String,
    val lastMessageAt: Instant,
    val lastReadAt: Instant?,
    val archivedAt: Instant?,
    val createdAt: Instant,
) {
    val isRead = lastReadAt != null && lastReadAt >= lastMessageAt
}

@Serializable
@JvmInline
value class ChatId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}
