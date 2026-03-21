package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class EventStar(
    val eventId: EventId,
    val value: InterestType?,
)

enum class InterestType {
    Star,
    Calendar,
}