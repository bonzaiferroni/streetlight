package streetlight.model.data

import kampfire.api.Username
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Feedback(
    val feedbackId: FeedbackId,
    val feedbackType: FeedbackType,
    val username: Username?,
    val text: String,
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

enum class FeedbackType {
    General,
    Issue
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
    val feedbackId: FeedbackId?,
    val feedbackType: FeedbackType,
    val text: String,
    val platform: Platform,
    val isPrivate: Boolean,
) {
    companion object {
        val Empty = FeedbackEdit(
            feedbackId = null,
            feedbackType = FeedbackType.General,
            text = "",
            platform = Platform.Web,
            isPrivate = true
        )
    }

    val isValid get() = text.isNotBlank()
}