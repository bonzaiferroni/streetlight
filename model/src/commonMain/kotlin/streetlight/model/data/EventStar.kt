package streetlight.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventStar(
    val eventId: EventId,
    val value: StarType?,
)

enum class StarType {
    Star,
    Calendar,
}