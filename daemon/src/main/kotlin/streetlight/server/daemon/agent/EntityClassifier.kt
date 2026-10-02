package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import kampfire.model.Outcome
import streetlight.model.data.EventSubtype
import streetlight.model.data.EventType
import streetlight.model.data.PropertyMap

/**
 * The crawler's room for classifying what it reads, by embeddings from [client] compared against the vectors of each
 * type, [typeVectors], and of each subtype, [subtypeVectors].
 */
class EntityClassifier(
    private val client: EmbeddingsClient,
    private val typeVectors: Map<EventType, Vector>,
    private val subtypeVectors: Map<EventSubtype, Vector>,
) {
    /** Reads the [EventType] of the [event] its properties describe. */
    suspend fun readEventType(event: PropertyMap): Outcome<EventType> = EmbeddingsProblem.Unspecified

    /** Reads the [EventSubtype] of the [event] its properties describe, among those that fit [eventType] when known. */
    suspend fun readEventSubtype(event: PropertyMap, eventType: EventType?): Outcome<EventSubtype> =
        EmbeddingsProblem.Unspecified
}
