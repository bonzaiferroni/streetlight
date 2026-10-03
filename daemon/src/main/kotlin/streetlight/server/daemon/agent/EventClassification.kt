package streetlight.server.daemon.agent

import kotlinx.serialization.Serializable
import streetlight.model.data.EventTag

/**
 * The classification of the event titled [title], read with the schema.org [declaredType] its page gave it: the tags
 * most likely its own, [rankedTags], the most likely first, and the [tags] given to it.
 */
@Serializable
data class EventClassification(
    val title: String?,
    val declaredType: String?,
    val rankedTags: List<RankedTag>,
    val tags: List<EventTag>,
)

/** A [tag] with the [probability] that an event carries it. */
@Serializable
data class RankedTag(
    val tag: EventTag,
    val probability: Double,
)
