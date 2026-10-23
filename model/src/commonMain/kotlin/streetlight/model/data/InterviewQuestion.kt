package streetlight.model.data

import kampfire.api.Markdown
import kotlinx.serialization.Serializable

/** A [question] asked of the author of a record, and their [answer]. */
@Serializable
data class InterviewQuestion(
    val question: String,
    val answer: Markdown,
)
