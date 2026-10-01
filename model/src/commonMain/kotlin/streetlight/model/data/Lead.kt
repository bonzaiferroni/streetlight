package streetlight.model.data

import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A page the crawler is given to read, and what is known of it before it is fetched. */
sealed interface Lead {
    val initialUrl: Url
    val leadType: LeadType
    val content: String? get() = null

    /** Whether the page may be a platform's rather than the source's, such as a page of Meetup. */
    val isExternalOrigin: Boolean
}

/** The page of one event, found in [feed], with the [feedEvent] the feed already showed of it. */
data class EventPage(
    override val initialUrl: Url,
    val feed: EventFeed,
    val feedEvent: PropertyMap?,
) : Lead {
    override val leadType get() = LeadType.EventPage
    override val isExternalOrigin get() = feed.isExternalOrigin
}

/**
 * A url submitted as the page of a location, read to create the location it describes, or to fill what the stored
 * location [locationId] lacks when given.
 */
@Serializable
data class LocationLead(
    val leadId: LeadId?,
    override val initialUrl: Url,
    override val content: String?,
    val checkedAt: Instant?,
    val createdAt: Instant,
    val locationId: LocationId? = null,
) : Lead {
    override val leadType get() = LeadType.Location
    override val isExternalOrigin get() = false
}

/** A url submitted as the page of one event, read to create the event, and its location when it is new. */
@Serializable
data class EventLead(
    val leadId: LeadId,
    override val initialUrl: Url,
    override val content: String?,
    val checkedAt: Instant?,
    val createdAt: Instant,
) : Lead {
    override val leadType get() = LeadType.Event
    override val isExternalOrigin get() = true
}

/**
 * A lead a user submits: a [url] of a [leadType], from the galaxy [galaxyId] when there is one, with the page's
 * html as [content] when the user provides it.
 */
@Serializable
data class StarLead(
    val url: Url,
    val leadType: LeadType,
    val galaxyId: GalaxyId? = null,
    val content: String? = null,
)

/** The kind of a lead, stored by ordinal. */
enum class LeadType {
    EventPage,
    EventFeed,
    Location,
    Event,

    /** A page listing events, read once like an event feed and not checked again. */
    EventScan,
}

@JvmInline
@Serializable
value class LeadId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}