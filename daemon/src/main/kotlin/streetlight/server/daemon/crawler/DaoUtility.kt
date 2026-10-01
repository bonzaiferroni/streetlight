package streetlight.server.daemon.crawler

import kampfire.model.Url
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import streetlight.model.data.EventPage
import streetlight.model.data.EventLead
import streetlight.model.data.Lead
import streetlight.model.data.LocationLead
import streetlight.model.data.GeneralEventFeed
import streetlight.model.data.Link
import streetlight.model.data.LinkAlias
import streetlight.model.data.LinkAliasId
import streetlight.model.data.LinkAccess
import streetlight.model.data.LinkContent
import streetlight.model.data.LinkId
import streetlight.model.data.LocationEventFeed
import streetlight.model.data.OriginId
import streetlight.model.data.ParseOutcome
import streetlight.model.data.toOriginId
import streetlight.server.model.DaoFacade
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Up to [limit] leads due a read: the locations' own feeds first, then the stored leads, then the websites of locations
 * never read there, each filling what room remains, oldest first.
 */
suspend fun DaoFacade.readCheckable(interval: Duration, limit: Int): List<Lead> {
    val feeds = location.readCheckableFeeds(interval, limit)
    val leads = feeds + if (feeds.size < limit) lead.readCheckable(interval, limit - feeds.size) else emptyList()
    return leads + if (leads.size < limit) location.readCheckableWebsites(limit - leads.size) else emptyList()
}

/** Marks [lead] as checked now. */
suspend fun DaoFacade.updateCheckedAt(lead: Lead) {
    when (lead) {
        is LocationEventFeed -> location.updateCheckedFeedAt(lead.location.locationId)
        is GeneralEventFeed -> this.lead.updateCheckedAt(lead.leadId)
        is LocationLead -> when (val locationId = lead.locationId) {
            null -> lead.leadId?.let { this.lead.updateCheckedAt(it) }
            else -> location.updateCheckedAt(locationId)
        }
        is EventLead -> this.lead.updateCheckedAt(lead.leadId)
        is EventPage -> { }
    }
}

/**
 * Records the fetch of [document] on the link of its served url, creating the link and its origin when missing, with
 * its lead's url as an alias when it differs.
 */
suspend fun DaoFacade.registerFetch(document: FetchDocument) {
    val initialUrl = document.lead.initialUrl
    val servedUrl = document.servedUrl
    val originId = servedUrl.toOriginId() ?: document.origin.originId
    if (originId != document.origin.originId) origin.readOrCreateOrigin(originId)

    suspendTransaction {
        val now = Clock.System.now()
        val existing = link.readLink(servedUrl) ?: link.readLink(initialUrl)
        val record = existing?.copy(fetchedAt = document.fetchedAt)?.also {
            link.updateLink(it)
        } ?: Link(
            linkId = LinkId(Uuid.random()), originId = originId, url = servedUrl, schemaType = null,
            fetchedAt = document.fetchedAt, createdAt = now, access = LinkAccess.Granted,
        ).also {
            link.createLink(it)
        }

        fun Url.toLinkAlias() = LinkAlias(LinkAliasId(Uuid.random()), record.linkId, this, now)

        link.createAliasIgnore(servedUrl.toLinkAlias())
        if (initialUrl != servedUrl) link.createAliasIgnore(initialUrl.toLinkAlias())
    }
}

/** Records [record] on the link for its url, creating the link when missing. */
suspend fun DaoFacade.recordLink(record: LinkRecord) {
    val existingLink = link.readLink(record.url)
    if (existingLink != null) {
        link.updateLink(existingLink.copy(
            access = record.access,
            content = record.content,
            schemaType = record.schemaType,
            parseOutcome = record.parseOutcome,
        ))
        return
    }
    val originId = record.url.toOriginId() ?: return
    origin.readOrCreateOrigin(originId)
    val now = Clock.System.now()
    val newLink = Link(
        linkId = LinkId(Uuid.random()), originId = originId, url = record.url, schemaType = record.schemaType,
        fetchedAt = now, createdAt = now, access = record.access, content = record.content,
        parseOutcome = record.parseOutcome,
    )
    link.createLink(newLink)
    link.createAliasIgnore(LinkAlias(LinkAliasId(Uuid.random()), newLink.linkId, record.url, now))
}

/** Whether an event page's link calls for another read: served, and never classified or awaiting scripting. */
fun Link.wantsRead() = access == LinkAccess.Granted && parseOutcome == null &&
    (content == null || content == LinkContent.Unread)

/** Whether a feed's link stops it from being read again. */
fun Link.stopsFeed() = access != LinkAccess.Granted || content in feedStopContent || parseOutcome == ParseOutcome.Fail

private val feedStopContent = setOf(LinkContent.OffSchema, LinkContent.OffScope, LinkContent.Unknown)
