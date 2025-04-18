package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlin.time.Duration

@Serializable
data class Event(
    val id: Long,
    val locationId: Int,
    val userId: Long,
    val currentRequestId: Long?,
    val url: String?,
    val imageUrl: String?,
    val streamUrl: String?,
    val name: String?,
    val description: String?,
    val status: EventStatus,
    val cashTips: Float?,
    val cardTips: Float?,
    val hours: Duration?,
    val startsAt: Instant,
)