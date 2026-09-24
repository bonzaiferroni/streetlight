package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.api.toMarkdown
import kampfire.model.Labeled
import streetlight.model.ui.Screen
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A bug report, with the device it came from. */
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

/** The fields of a bug report as it is sent. */
@Serializable
data class BugEdit(
    val bugId: BugId? = null,
    val description: Markdown = "".toMarkdown(),
    val platform: Platform,
    val screen: Screen? = null,
    val path: String? = null,
    val deviceAgent: String? = null,
) {
    val isValid get() = description.isNotBlank()
}
