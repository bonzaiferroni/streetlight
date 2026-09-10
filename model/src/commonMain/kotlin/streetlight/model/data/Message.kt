package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.TimeCursor
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Message(
    val messageId: MessageId,
    val chatId: ChatId,
    val author: Username,
    val content: Markdown,
    val sentAt: Instant,
): OmniRecord {
    override val text get() = "$author sent you a message"
    override val recordAt get() = sentAt
}

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
    val badges: List<StarBadge>,
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

@Serializable
data class ChatMessageRequest(
    val chatId: ChatId,
    val cursor: TimeCursor? = null,
)

@Serializable
data class ChatRequest(
    val isArchive: Boolean,
    val cursor: TimeCursor? = null
)