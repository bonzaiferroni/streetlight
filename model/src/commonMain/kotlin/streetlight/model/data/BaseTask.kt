package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

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

enum class ContentStatus {
    Submitted,
    Accepted,
    Rejected,
}

enum class TaskStatus {
    Requested,
    Accepted,
    Completed,
    Dismissed,
}

enum class FeedStatus {
    Reviewing,
    Live,
    Moderating,
    Removed,
}

@Serializable
sealed interface StarTask

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

@Serializable
sealed interface TaskContent: Labeled

@Serializable
data class QuorumReviewContent(
    val quorum: Quorum,
    val task: QuorumTask,
    val editLog: EditLog
): TaskContent {
    override val label get() = quorum.question.label
}

@Serializable
data class EditTaskContent(
    val task: EditTask,
    val editLog: EditLog,
): TaskContent {
    override val label get() = "edit a ${editLog.recordType}"
}