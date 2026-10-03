package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
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
    /**
     * The [event] with its type and subtype, each left unset when it is not similar enough, read with the schema.org
     * [declaredType] its page gave it, when it gave one. [onClassified] is given the classification, whatever passed.
     */
    suspend fun classifyEvent(
        event: EventEdit,
        declaredType: String? = null,
        onClassified: (EventClassification) -> Unit = {},
    ): EventEdit {
        val eventVector = client.embedQuery(eventInstruction, event.getEmbeddingsText(declaredType))
            .toDataOr { return event }

        val typeRanking = rankBySimilarity(eventVector, typeVectors)
        val (eventType, typeSimilarity) = typeRanking.first()
        val runnerUp = typeRanking.getOrNull(1)
        val (eventSubtype, subtypeSimilarity) = rankBySimilarity(eventVector, subtypeVectors).first()
        val classification = EventClassification(
            title = event.title,
            declaredType = declaredType,
            eventType = eventType,
            typeSimilarity = typeSimilarity,
            runnerUpType = runnerUp?.first,
            runnerUpSimilarity = runnerUp?.second,
            eventSubtype = eventSubtype,
            subtypeSimilarity = subtypeSimilarity,
            typeAccepted = typeSimilarity >= minTypeSimilarity,
            subtypeAccepted = subtypeSimilarity >= minSubtypeSimilarity,
        )
        onClassified(classification)

        return event.copy(
            eventType = eventType.takeIf { classification.typeAccepted },
            eventSubtype = eventSubtype.takeIf { classification.subtypeAccepted },
        )
    }
}

/** Each of [vectors] with its cosine similarity to [eventVector], the most similar first. */
private fun <T> rankBySimilarity(eventVector: Vector, vectors: Map<T, Vector>): List<Pair<T, Double>> = vectors
    .map { (value, vector) -> value to eventVector.cosineSimilarity(vector) }
    .sortedByDescending { (_, similarity) -> similarity }

internal const val eventInstruction = "Given the details of a local event, retrieve the category that best describes it"
private const val minTypeSimilarity = 0.4
private const val minSubtypeSimilarity = 0.5
