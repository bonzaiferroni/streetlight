package streetlight.model.data

import kampfire.api.Username
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Message(
    val messageId: MessageId,
    val chainId: MessageId,
    val parentId: MessageId?,
    val author: Username,
    val subject: String?,
    val text: String,
    val isArchived: Boolean,
    val isRead: Boolean,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class MessageId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}