package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A task a user takes up on a record, such as reviewing an edit, with its decision and status. */
@Serializable
data class BaseTask(
    val taskId: TaskId,
    val starId: StarId,
    val recordId: Uuid,
    val recordType: RecordType,
    val decision: Int?,
    val taskStatus: TaskStatus,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class TaskId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}

/** The review status of a piece of content. */
enum class ContentStatus {
    Submitted,
    Accepted,
    Rejected,
}

/** The status of a task. */
enum class TaskStatus {
    Requested,
    Accepted,
    Completed,
    Dismissed,
}

/** Whether a post is live in its galaxy's feed or waiting for review. */
enum class FeedStatus {
    Reviewing,
    Live,
    Moderating,
    Removed,
}

/** A task a user can take up. */
@Serializable
sealed interface StarTask

/** A task to answer the question of a [Quorum]. */
@Serializable
data class QuorumTask(
    val taskId: TaskId,
    val recordId: Uuid,
    val starId: StarId,
    val decision: Int?,
    val taskStatus: TaskStatus,
    val updatedAt: Instant,
    val createdAt: Instant,
): StarTask {
    val quorumId get() = QuorumId(recordId)
}

/** A task to review an edit. */
@Serializable
data class EditTask(
    val taskId: TaskId,
    val recordId: Uuid,
    val starId: StarId,
    val taskStatus: TaskStatus,
    val updatedAt: Instant,
    val createdAt: Instant,
): StarTask {
    val editLogId get() = EditLogId(recordId)
}

/** What a task view shows for a task. */
@Serializable
sealed interface TaskContent: Labeled

/** The content of a quorum task: the question and the record it is about. */
@Serializable
data class QuorumReviewContent(
    val quorum: Quorum,
    val task: QuorumTask,
    val editLog: EditLog
): TaskContent {
    override val label get() = quorum.question.label
}

/** The content of an edit task: the edit and the record it changes. */
@Serializable
data class EditTaskContent(
    val task: EditTask,
    val editLog: EditLog,
): TaskContent {
    override val label get() = "edit a ${editLog.recordType}"
}