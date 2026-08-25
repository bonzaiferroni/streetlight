package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.api.toMarkdown
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Message(
    val messageId: MessageId,
    val chainId: MessageId,
    val parentId: MessageId?,
    val author: Username,
    val recipient: Username,
    val subject: String?,
    val content: Markdown,
    val isArchived: Boolean,
    val isRead: Boolean,
    val createdAt: Instant,
) {

}

@Serializable
@JvmInline
value class MessageId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}

@Serializable
data class MessageEdit(
    val messageId: MessageId,
    val chainId: MessageId,
    val parentId: MessageId?,
    val recipient: Username,
    val subject: String?,
    val content: Markdown,
) {
    val isValid get() = content.value.isNotBlank()
}