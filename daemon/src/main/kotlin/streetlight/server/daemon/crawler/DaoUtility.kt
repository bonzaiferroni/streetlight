package streetlight.server.daemon.crawler

import kampfire.model.Url
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import streetlight.model.data.EventFeed
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

/** The feeds not checked within [interval]: the locations' own first, then the general feeds, each oldest first. */
suspend fun DaoFacade.readCheckable(interval: Duration): List<EventFeed> =
    location.readCheckableFeeds(interval) + eventFeed.readCheckable(interval)

/** Marks [lead] as checked now. */
suspend fun DaoFacade.updateCheckedAt(lead: EventFeed) {
    when (lead) {
        is LocationEventFeed -> location.updateCheckedAt(lead.location.locationId)
        is GeneralEventFeed -> eventFeed.updateCheckedAt(lead.eventFeedId)
    }
}

/** Records the fetch of [document] on the link of its lead's url, creating it when missing, with its served url as an alias. */
suspend fun DaoFacade.registerFetch(document: FetchDocument) {
    val initialUrl = document.lead.initialUrl
    val servedUrl = document.servedUrl
    val originId = document.origin.originId

    suspendTransaction {
        val now = Clock.System.now()
        val existing = link.readLinkByAlias(initialUrl) ?: link.readLink(initialUrl)
        val record = existing?.copy(fetchedAt = document.fetchedAt)?.also {
            link.updateLink(it)
        } ?: Link(
            linkId = LinkId(Uuid.random()), originId = originId, url = initialUrl, schemaType = null,
            fetchedAt = document.fetchedAt, createdAt = now, access = LinkAccess.Granted,
        ).also {
            link.createLink(it)
        }

        fun Url.toLinkAlias() = LinkAlias(LinkAliasId(Uuid.random()), record.linkId, this, now)

        link.createAliasIgnore(initialUrl.toLinkAlias())
        link.createAliasIgnore(servedUrl.toLinkAlias())
    }
}

/** Records [record] on the link for its url, creating the link when missing. */
suspend fun DaoFacade.recordLink(record: LinkRecord) {
    val existing = link.readLinkByAlias(record.url) ?: link.readLink(record.url)
    if (existing != null) {
        link.updateLink(existing.copy(
            access = record.access,
            content = record.content,
            schemaType = record.schemaType,
            parseOutcome = record.parseOutcome,
            parseNote = record.note,
        ))
        return
    }
    val originId = record.url.toOriginId() ?: return
    origin.readOrCreateOrigin(originId)
    val now = Clock.System.now()
    val created = Link(
        linkId = LinkId(Uuid.random()), originId = originId, url = record.url, schemaType = record.schemaType,
        fetchedAt = now, createdAt = now, access = record.access, content = record.content,
        parseOutcome = record.parseOutcome, parseNote = record.note,
    )
    link.createLink(created)
    link.createAliasIgnore(LinkAlias(LinkAliasId(Uuid.random()), created.linkId, record.url, now))
}

/** Whether an event page's link calls for another read: served, and never classified or awaiting scripting. */
fun Link.wantsRead() = access == LinkAccess.Granted && parseOutcome == null &&
    (content == null || content == LinkContent.Unread)

/** Whether a feed's link stops it from being read again. */
fun Link.stopsFeed() = access != LinkAccess.Granted || content in feedStopContent || parseOutcome == ParseOutcome.Fail

private val feedStopContent = setOf(LinkContent.OffSchema, LinkContent.OffScope, LinkContent.Unknown)
