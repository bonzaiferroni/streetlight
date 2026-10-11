package streetlight.server.daemon.agent

import kotlinx.serialization.Serializable
import streetlight.model.data.EventTag
import streetlight.model.data.LocationTag

/**
 * The classification of the event titled [title], read with the schema.org [ldType] its page gave it: the tags
 * most likely its own, [rankedTags], the most likely first, and the [tags] given to it.
 */
@Serializable
data class EventClassification(
    val title: String?,
    val ldType: String?,
    val rankedTags: List<RankedTag<EventTag>>,
    val tags: List<EventTag>,
)

/**
 * The classification of the location named [name]: the tags most likely its own, [rankedTags], the most likely first,
 * and the [tags] given to it.
 */
@Serializable
data class LocationClassification(
    val name: String?,
    val rankedTags: List<RankedTag<LocationTag>>,
    val tags: List<LocationTag>,
)

/** A [tag] with the [probability] that an entity carries it. */
@Serializable
data class RankedTag<T>(
    val tag: T,
    val probability: Double,
)
