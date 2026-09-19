package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.api.toMarkdown
import kampfire.model.Labeled
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Feedback(
    val feedbackId: FeedbackId,
    val feedbackType: FeedbackType,
    val username: Username?,
    val text: Markdown,
    val platform: Platform,
    val isPrivate: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class FeedbackId(override val value: Uuid): RecordId {
    companion object {
        fun random() = FeedbackId(Uuid.random())
    }
}

enum class FeedbackType(override val label: String): Labeled {
    General("General Feedback"),
    Suggestion("Suggestion"),
    Issue("Issue or Bug");
}

enum class Platform {
    Web,
    Android,
    IOS,
    Linux,
    Windows,
    MacOS,
}

@Serializable
data class FeedbackEdit(
    val feedbackId: FeedbackId? = null,
    val feedbackType: FeedbackType = FeedbackType.General,
    val text: Markdown = "".toMarkdown(),
    val platform: Platform,
    val deviceAgent: String? = null,
    val isPrivate: Boolean = true,
) {
    val isValid get() = text.isNotBlank()
}