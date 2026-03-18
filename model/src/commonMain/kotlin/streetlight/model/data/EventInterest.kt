package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class EventInterest(
    val eventId: EventId,
    val value: InterestType?,
)

enum class InterestType {
    Star,
    Calendar,
}