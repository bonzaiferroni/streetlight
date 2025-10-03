package streetlight.model.data

import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

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
value class RequestId(override val value: String): ProjectId {
    companion object { fun random() = RequestId(randomUuidString()) }
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