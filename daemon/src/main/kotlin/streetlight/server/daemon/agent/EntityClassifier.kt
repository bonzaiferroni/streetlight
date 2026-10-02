package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
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
    /** The [event] with its type, left unset when it cannot be classified. */
    suspend fun classifyEvent(event: EventEdit): EventEdit {
        val eventVector = client.embedQuery(eventInstruction, event.getEmbeddingsText()).toDataOr { return event }

        val eventType = readEventType(eventVector).toDataOrNull()
        return event.copy(eventType = eventType)
    }

    /** Reads the [EventType] whose vector is most similar to [eventVector], when it is similar enough. */
    private fun readEventType(eventVector: Vector): Outcome<EventType> {
        val (eventType, similarity) = typeVectors
            .map { (eventType, typeVector) -> eventType to eventVector.cosineSimilarity(typeVector) }
            .maxBy { (_, similarity) -> similarity }
        if (similarity < minTypeSimilarity) return EmbeddingsProblem.NoMatch
        return Ok(eventType)
    }
}

private const val eventInstruction = "Given the details of a local event, retrieve the category that best describes it"
private const val minTypeSimilarity = 0.4
