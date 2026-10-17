package streetlight.model.data

import kotlinx.serialization.Serializable

/** The progress of the crawler in gathering a location's events from its events page. */
@Serializable
sealed interface GatherEventStatus {
    /** The location has no events url. */
    @Serializable
    data object Url: GatherEventStatus

    /** The events page has not been read yet. */
    @Serializable
    data object Unchecked: GatherEventStatus

    /** The events page was read, but no events could be gathered from it. */
    @Serializable
    data class Blocked(
        val access: LinkAccess,
        val content: LinkContent?,
        val outcome: ParseOutcome?,
    ): GatherEventStatus

    /** The events page was parsed with [outcome], and [events] are the latest gathered. */
    @Serializable
    data class Ready(
        val outcome: ParseOutcome,
        val events: List<Event>,
    ): GatherEventStatus
}
