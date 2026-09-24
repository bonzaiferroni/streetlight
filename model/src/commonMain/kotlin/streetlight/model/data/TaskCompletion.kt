package streetlight.model.data

import kotlinx.serialization.Serializable

/** A user's answer to a task. */
@Serializable
sealed interface TaskCompletion

/** The answer to an edit task: the edit as reviewed, or `null` to reject it. */
@Serializable
data class EditCompletion(
    val taskId: TaskId,
    // val editLogId: EditLogId,
    val edit: RecordEdit?
): TaskCompletion