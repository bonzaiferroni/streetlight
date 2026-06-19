package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class EditLog(
    val editLogId: EditLogId,
    val recordId: Uuid,
    val username: String,
    val recordType: RecordType,
    val recordEdit: RecordEdit?,
    val editType: EditType,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class EditLogId(override val value: Uuid): RecordId {
}

@Serializable
sealed interface RecordEdit {
    val recordType: RecordType
}

enum class RecordType {
    Location,
    Event,
    Galaxy,
    Wiki,
}

enum class EditType {
    Create,
    Update,
    Delete,
}

enum class EditStatus {
    PendingReview,
    Accepted,
}

val EditType.verb get() = when(this) {
    EditType.Create -> "created"
    EditType.Update -> "updated"
    EditType.Delete -> "deleted"
}