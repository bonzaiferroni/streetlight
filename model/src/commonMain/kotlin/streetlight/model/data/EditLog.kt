package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class EditLog<Edit: RecordEdit>(
    val editLogId: EditLogId,
    val recordId: RecordId,
    val username: String,
    val recordType: RecordType,
    val recordEdit: Edit,
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