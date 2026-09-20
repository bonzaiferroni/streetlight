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
data class Bug(
    val bugId: BugId,
    val description: Markdown,
    val platform: Platform,
    val status: BugStatus,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class BugId(override val value: Uuid): RecordId {
    companion object {
        fun random() = BugId(Uuid.random())
    }
}

enum class BugStatus(override val label: String): Labeled {
    Open("Open"),
    Fixed("Fixed"),
    Dismissed("Dismissed");
}

@Serializable
data class BugEdit(
    val bugId: BugId? = null,
    val description: Markdown = "".toMarkdown(),
    val platform: Platform,
    val deviceAgent: String? = null,
) {
    val isValid get() = description.isNotBlank()
}
