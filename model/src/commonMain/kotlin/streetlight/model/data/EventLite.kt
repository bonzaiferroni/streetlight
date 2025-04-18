package streetlight.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class EventLite(
    val id: Int,
    val locationId: Int,
    val userId: Long,
    val url: String?,
    val imageUrl: String?,
    val streamUrl: String?,
    val name: String?,
    val description: String?,
    val status: EventStatus,
    val duration: Duration,
    val startsAt: Instant,
)