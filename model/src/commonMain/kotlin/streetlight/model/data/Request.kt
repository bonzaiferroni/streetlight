package streetlight.model.data

import kabinet.db.TableId
import kabinet.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Request(
    val requestId: RequestId,
    val eventId: EventId,
    val songId: SongId,
    val performed: Boolean,
    val notes: String,
    val requesterName: String?,
    val requestedAt: Instant,
)

@JvmInline
@Serializable
value class RequestId(override val value: String): ProjectId {
    companion object { fun random() = RequestId(randomUuidString()) }
}