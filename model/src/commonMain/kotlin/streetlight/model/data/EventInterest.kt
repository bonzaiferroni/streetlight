package streetlight.model.data

import kampfire.model.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventInterest(
    val userId: UserId,
    val eventId: EventId,
    val interestType: InterestType,
    val createdAt: Instant,
)

enum class InterestType {
    Star,
    Attend,
}