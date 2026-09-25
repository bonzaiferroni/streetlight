package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import streetlight.agent.FetchText
import streetlight.model.data.Link
import streetlight.model.data.LinkAlias
import streetlight.model.data.LinkAliasId
import streetlight.model.data.LinkId
import streetlight.model.data.OriginId
import streetlight.model.data.toOriginId
import streetlight.server.model.DaoFacade
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Records the fetch of a page as a link with its aliases, returning the page's url.
 *
 * The link belongs to the origin of the page's canonical or final url, which a redirect can place on another
 * host than [fetchOriginId], so that origin is created when missing.
 */
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
            fetchedAt = fetch.fetchedAt, createdAt = now,
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