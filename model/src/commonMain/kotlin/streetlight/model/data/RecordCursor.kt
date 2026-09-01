package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class RecordCursor(
    val recordId: Uuid,
    val recordAt: Instant
)