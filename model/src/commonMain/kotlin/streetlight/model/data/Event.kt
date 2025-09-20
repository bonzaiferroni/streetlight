package streetlight.model.data

import kabinet.db.TableId
import kabinet.model.UserId
import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlin.jvm.JvmInline
import kotlin.time.Duration

@Serializable
data class Event(
    val eventId: EventId,
    val locationId: LocationId,
    val userId: UserId,
    val currentRequestId: RequestId?,
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

@JvmInline
@Serializable
value class EventId(override val value: String): ProjectId {
    companion object { fun random() = EventId(randomUuidString()) }
}