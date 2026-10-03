package streetlight.server.daemon.agent

import kotlinx.serialization.Serializable
import streetlight.model.data.EventSubtype
import streetlight.model.data.EventType

/**
 * The classification of the event titled [title], read with the schema.org [declaredType] its page gave it: the most
 * similar [eventType] and [eventSubtype], the [runnerUpType] after it, and each one's similarity. [typeAccepted] and
 * [subtypeAccepted] are whether each passed its minimum similarity and was given to the event.
 */
@Serializable
data class EventClassification(
    val title: String?,
    val declaredType: String?,
    val eventType: EventType,
    val typeSimilarity: Double,
    val runnerUpType: EventType?,
    val runnerUpSimilarity: Double?,
    val eventSubtype: EventSubtype,
    val subtypeSimilarity: Double,
    val typeAccepted: Boolean,
    val subtypeAccepted: Boolean,
)
