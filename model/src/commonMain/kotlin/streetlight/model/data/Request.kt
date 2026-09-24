package streetlight.model.data

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A song requested at an event, by name or to join in playing. */
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

/** A song request as it is sent, naming a known song or a new one. */
@Serializable
data class NewRequest(
    val eventId: EventId,
    val songId: SongId?,
    val songName: String? = null,
    val isJoining: Boolean,
    val comment: String?,
    val requesterName: String?,
)

/** A song and how often it was played. */
@Serializable
data class RequestItem(
    val song: Song,
    val plays: Int,
)