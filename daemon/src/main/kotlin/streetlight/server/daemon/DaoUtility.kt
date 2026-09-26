package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import streetlight.agent.FetchText
import streetlight.model.data.Link
import streetlight.model.data.LinkAlias
import streetlight.model.data.LinkAliasId
import streetlight.model.data.LinkAccess
import streetlight.model.data.LinkContent
import streetlight.model.data.LinkId
import streetlight.model.data.OriginId
import streetlight.model.data.ParseOutcome
import streetlight.model.data.toOriginId
import streetlight.server.model.DaoFacade
import kotlin.time.Clock
import kotlin.uuid.Uuid

/** Records the fetch of a page as a link with its aliases, creating its origin when missing, and returns the page's url. */
suspend fun DaoFacade.registerFetch(
    fetchOriginId: OriginId,
    doc: Document,
    fetch: FetchText,
): Url {
    val canonicalUrl = doc.readCanonicalUrl(fetch.pageUrl)
    val visitedUrl = fetch.pageUrl.normalize()
    val pageUrl = canonicalUrl ?: visitedUrl
    val originId = pageUrl.toOriginId() ?: fetchOriginId
    if (originId != fetchOriginId) origin.readOrCreateOrigin(originId)

    suspendTransaction {
        val url = canonicalUrl ?: visitedUrl
        val now = Clock.System.now()
        val record = link.readLink(url)?.copy(fetchedAt = fetch.fetchedAt)?.also {
            link.updateLink(it)
        } ?: Link(
            linkId = LinkId(Uuid.random()), originId = originId, url = url, schemaType = null,
            fetchedAt = fetch.fetchedAt, createdAt = now, access = LinkAccess.Granted,
        ).also {
            link.createLink(it)
        }

        fun Url.toLinkAlias() = LinkAlias(LinkAliasId(Uuid.random()), record.linkId, this, now)

        link.createAliasIgnore(fetch.fetchUrl.toLinkAlias())
        link.createAliasIgnore(visitedUrl.toLinkAlias())

        canonicalUrl?.let {
            link.createAliasIgnore(canonicalUrl.toLinkAlias())
        }
    }

    return pageUrl
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
