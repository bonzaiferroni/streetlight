package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import kampfire.model.Outcome
import streetlight.model.data.EventSubtype
import streetlight.model.data.EventType
import streetlight.model.data.RawEntity

/**
 * The crawler's room for classifying what it reads, by embeddings from [client] compared against the vectors of each
 * type, [typeVectors], and of each subtype, [subtypeVectors].
 */
class EntityClassifier(
    private val client: EmbeddingsClient,
    private val typeVectors: Map<EventType, Vector>,
    private val subtypeVectors: Map<EventSubtype, Vector>,
) {
    /** Reads the [EventType] of [rawEvent]. */
    suspend fun readEventType(rawEvent: RawEntity): Outcome<EventType> = EmbeddingsProblem.Unspecified

    /** Reads the [EventSubtype] of [rawEvent], among those that fit [eventType] when known. */
    suspend fun readEventSubtype(rawEvent: RawEntity, eventType: EventType?): Outcome<EventSubtype> =
        EmbeddingsProblem.Unspecified
}
