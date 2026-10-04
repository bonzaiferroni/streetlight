package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import kampfire.model.toDataOr
import streetlight.model.data.EventEdit
import streetlight.model.data.EventTag

/**
 * The crawler's room for classifying what it reads, by embeddings from [client], centered on [center], and the model
 * of each tag, [tagModels].
 */
class EntityClassifier(
    private val client: EmbeddingsClient,
    private val center: DoubleArray,
    private val tagModels: Map<EventTag, TagModel>,
) {
    /**
     * The [event] with the tags it is given, read with the schema.org [ldType] its page gave it, when it gave
     * one. [onClassified] is given the classification.
     */
    suspend fun classifyEvent(
        event: EventEdit,
        ldType: String? = null,
        onClassified: (EventClassification) -> Unit = {},
    ): EventEdit {
        val eventVector = client.embedQuery(eventInstruction, event.getEmbeddingsText(ldType))
            .toDataOr { return event }

        val classification = readTags(eventVector, event.title, ldType)
        onClassified(classification)

        return event.copy(tags = classification.tags)
    }

    /**
     * The classification of the event of [eventVector], titled [title] and read with [ldType]: each tag whose
     * probability reaches the minimum is given.
     */
    internal fun readTags(eventVector: Vector, title: String? = null, ldType: String? = null): EventClassification {
        val features = eventVector.toFeatures(center)
        val rankedTags = tagModels
            .map { (tag, model) -> RankedTag(tag, model.readProbability(features)) }
            .sortedByDescending { it.probability }
        return EventClassification(
            title = title,
            ldType = ldType,
            rankedTags = rankedTags.take(reportedTagCount),
            tags = rankedTags.filter { it.probability >= minProbability }.map { it.tag },
        )
    }
}

internal const val eventInstruction = "Given the details of a local event, retrieve the category that best describes it"
private const val reportedTagCount = 5
private const val minProbability = 0.7
