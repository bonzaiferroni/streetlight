package streetlight.model.data

import kampfire.model.Url

/** A page the crawler is given to read, and what is known of it before it is fetched. */
sealed interface Lead {
    val initialUrl: Url
    val leadType: LeadType
}

/** The page of one event, found in [feed], with the [feedEvent] the feed already showed of it. */
data class EventPage(
    override val initialUrl: Url,
    val feed: EventFeed,
    val feedEvent: RawEvent?,
) : Lead {
    override val leadType get() = LeadType.EventPage
}

enum class LeadType {
    EventPage,
    EventFeed,
}