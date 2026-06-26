package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface TaskCompletion

@Serializable
data class EditCompletion(
    val taskId: TaskId,
    // val editLogId: EditLogId,
    val edit: RecordEdit?
): TaskCompletion