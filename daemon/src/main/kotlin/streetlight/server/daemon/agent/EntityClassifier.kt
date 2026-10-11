package streetlight.server.daemon.agent

import ai.koog.embeddings.base.Vector
import kampfire.model.toDataOr
import streetlight.model.data.EventEdit
import streetlight.model.data.EventTag
import streetlight.model.data.LocationEdit
import streetlight.model.data.LocationTag

/**
 * The crawler's room for classifying what it reads, by embeddings from [client] and the tag models of events,
 * [eventModels], and of locations, [locationModels].
 */
class EntityClassifier(
    private val client: EmbeddingsClient,
    private val eventModels: TagModelSet<EventTag>,
    private val locationModels: TagModelSet<LocationTag>,
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

        val classification = readEventTags(eventVector, event.title, ldType)
        onClassified(classification)

        return event.copy(tags = classification.tags)
    }

    /** The [location] with the tags it is given. [onClassified] is given the classification. */
    suspend fun classifyLocation(
        location: LocationEdit,
        onClassified: (LocationClassification) -> Unit = {},
    ): LocationEdit {
        val locationVector = client.embedQuery(locationInstruction, location.getEmbeddingsText())
            .toDataOr { return location }

        val classification = readLocationTags(locationVector, location.name)
        onClassified(classification)

        return location.copy(tags = classification.tags)
    }

    /**
     * The classification of the event of [eventVector], titled [title] and read with [ldType]: each tag whose
     * probability reaches the minimum is given.
     */
    internal fun readEventTags(eventVector: Vector, title: String? = null, ldType: String? = null): EventClassification {
        val rankedTags = eventModels.rankTags(eventVector)
        return EventClassification(
            title = title,
            ldType = ldType,
            rankedTags = rankedTags.take(reportedTagCount),
            tags = rankedTags.filter { it.probability >= minProbability }.map { it.tag },
        )
    }

    /** The classification of the location of [locationVector], named [name]: each tag whose probability reaches the minimum is given. */
    internal fun readLocationTags(locationVector: Vector, name: String? = null): LocationClassification {
        val rankedTags = locationModels.rankTags(locationVector)
        return LocationClassification(
            name = name,
            rankedTags = rankedTags.take(reportedTagCount),
            tags = rankedTags.filter { it.probability >= minProbability }.map { it.tag },
        )
    }
}

internal const val eventInstruction = "Given the details of a local event, retrieve the category that best describes it"
internal const val locationInstruction = "Given the details of a local place, retrieve the category that best describes it"
private const val reportedTagCount = 5
private const val minProbability = 0.7
