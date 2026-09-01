package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class RecordCursor(
    val recordId: Uuid,
    val recordAt: Instant,
    val limit: Int = DefaultLimit,
) {
    companion object {
        val DefaultLimit = 15
    }
}

val RecordCursor?.limitOrDefault get() = this?.limit ?: RecordCursor.DefaultLimit