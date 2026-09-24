package streetlight.model.data

import kampfire.model.Labeled
import kampfire.utils.pascalToTitle
import koala.Image
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A logged edit to a record: who made it, what it changed, and whether it was reviewed. */
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
    override fun toString() = value.toString()
}

/** An edit of a record, labeled for its log. */
@Serializable
sealed interface RecordEdit {
    val recordType: RecordType
    val label: String
    val image: Image? get() = null
}

/** The kinds of record that can be edited, flagged or reviewed. */
enum class RecordType: Labeled {
    Location,
    Event,
    Galaxy,
    Wiki,
    EditLog,
    Quorum;

    override val label = name.pascalToTitle()
    override fun toString() = label
}

/** Whether an edit created, updated or deleted a record. */
enum class EditType {
    Create,
    Update,
    Delete,
}

/** The review status of an edit. */
enum class EditStatus {
    PendingReview,
    Accepted,
}

/** The past-tense verb of the edit, for its log. */
val EditType.verb get() = when(this) {
    EditType.Create -> "created"
    EditType.Update -> "updated"
    EditType.Delete -> "deleted"
}