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
import streetlight.server.db.services.LinkTableDao
import kotlin.time.Clock
import kotlin.uuid.Uuid

suspend fun LinkTableDao.registerFetch(
    fetchOriginId: OriginId,
    doc: Document,
    fetch: FetchText,
): Url {
    val canonicalUrl = doc.readCanonicalUrl(fetch.pageUrl)
    val visitedUrl = fetch.pageUrl.normalize()
    val pageUrl = canonicalUrl ?: visitedUrl
    val originId = pageUrl.toOriginId() ?: fetchOriginId

    suspendTransaction {
        val url = canonicalUrl ?: visitedUrl
        val now = Clock.System.now()
        val link = readLink(url)?.copy(fetchedAt = fetch.fetchedAt)?.also {
            updateLink(it)
        } ?: Link(
            linkId = LinkId(Uuid.random()), originId = originId, url = url, schemaType = null,
            fetchedAt = fetch.fetchedAt, createdAt = now,
        ).also { link ->
            createLink(link)
        }

        fun Url.toLinkAlias() = LinkAlias(LinkAliasId(Uuid.random()), link.linkId, this, now)

        createAliasIgnore(fetch.fetchUrl.toLinkAlias())
        createAliasIgnore(visitedUrl.toLinkAlias())

        canonicalUrl?.let {
            createAliasIgnore(canonicalUrl.toLinkAlias())
        }
    }

    return pageUrl
}