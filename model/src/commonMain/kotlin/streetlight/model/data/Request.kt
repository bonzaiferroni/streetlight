package streetlight.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val id: Long,
    val eventId: Long,
    val songId: Long,
    val performed: Boolean,
    val notes: String,
    val requesterName: String?,
    val requestedAt: Instant,
)