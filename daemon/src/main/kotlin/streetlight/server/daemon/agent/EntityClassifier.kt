package streetlight.server.daemon.agent

import kampfire.model.Outcome
import streetlight.model.data.EventSubtype
import streetlight.model.data.EventType
import streetlight.model.data.PropertyMap

/** The crawler's room for classifying what it reads, by embeddings from [client]. */
class EntityClassifier(
    private val client: EmbeddingsClient,
) {
    /** Reads the [EventType] of the [event] its properties describe. */
    suspend fun readEventType(event: PropertyMap): Outcome<EventType> = EmbeddingsProblem.Unspecified

    /** Reads the [EventSubtype] of the [event] its properties describe, among those that fit [eventType] when known. */
    suspend fun readEventSubtype(event: PropertyMap, eventType: EventType?): Outcome<EventSubtype> =
        EmbeddingsProblem.Unspecified
}
