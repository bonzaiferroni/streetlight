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
}

/** The page of one event, found in [feed], with the [feedEvent] the feed already showed of it. */
data class EventPage(
    override val initialUrl: Url,
    val feed: EventFeed,
    val feedEvent: PropertyMap?,
) : Lead {
    override val leadType get() = LeadType.EventPage
}

/** A url submitted as the page of a location, read to create the location it describes. */
@Serializable
data class LocationLead(
    val leadId: LeadId,
    override val initialUrl: Url,
    val checkedAt: Instant?,
    val createdAt: Instant,
) : Lead {
    override val leadType get() = LeadType.Location
}

/** A lead a user submits: a [url] of a [leadType], from the galaxy [galaxyId] when there is one. */
@Serializable
data class StarLead(
    val url: Url,
    val leadType: LeadType,
    val galaxyId: GalaxyId? = null,
)

/** The kind of a lead, stored by ordinal. */
enum class LeadType {
    EventPage,
    EventFeed,
    Location,
}

@JvmInline
@Serializable
value class LeadId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}