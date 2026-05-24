package streetlight.model.data

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Request(
    val requestId: RequestId,
    val eventId: EventId,
    val songId: SongId,
    val isJoining: Boolean,
    val comment: String?,
    val requesterName: String?,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class RequestId(override val value: Uuid): RecordId {
    companion object { fun random() = RequestId(Uuid.random()) }
}

@Serializable
data class NewRequest(
    val eventId: EventId,
    val songId: SongId?,
    val songName: String? = null,
    val isJoining: Boolean,
    val comment: String?,
    val requesterName: String?,
)

@Serializable
data class RequestItem(
    val song: Song,
    val plays: Int,
)