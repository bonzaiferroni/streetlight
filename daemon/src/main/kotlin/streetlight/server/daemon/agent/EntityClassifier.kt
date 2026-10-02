package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.toDataOr
import streetlight.model.data.EventEdit
import streetlight.model.data.EventSubtype
import streetlight.model.data.EventType

/**
 * The crawler's room for classifying what it reads, by embeddings from [client] compared against the vectors of each
 * type, [typeVectors], and of each subtype, [subtypeVectors].
 */
class EntityClassifier(
    private val client: EmbeddingsClient,
    private val typeVectors: Map<EventType, Vector>,
    private val subtypeVectors: Map<EventSubtype, Vector>,
) {
    /** Reads the [EventType] of [event], the most similar type when it is similar enough. */
    suspend fun readEventType(event: EventEdit): Outcome<EventType> {
        val eventVector = client.embedQuery(eventTypeInstruction, event.getEmbeddingsText()).toDataOr { return it }

        val (eventType, similarity) = typeVectors
            .map { (eventType, typeVector) -> eventType to eventVector.cosineSimilarity(typeVector) }
            .maxBy { (_, similarity) -> similarity }
        if (similarity < minTypeSimilarity) return EmbeddingsProblem.NoMatch
        return Ok(eventType)
    }

    /** Reads the [EventSubtype] of [event], among those that fit [eventType] when known. */
    suspend fun readEventSubtype(event: EventEdit, eventType: EventType?): Outcome<EventSubtype> =
        EmbeddingsProblem.Unspecified
}

private const val eventTypeInstruction = "Given the details of a local event, retrieve the category that best describes it"
private const val minTypeSimilarity = 0.4
