package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kampfire.model.toUrl
import streetlight.agent.LMProblem
import streetlight.agent.parseHtmlDocument
import streetlight.agent.readHtml
import streetlight.agent.tryQuery
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.Origin
import streetlight.model.data.SchemaType
import streetlight.model.data.toOriginId
import streetlight.server.model.ContentParse
import streetlight.server.routes.SchemaParserText

class EventFeedReader(
    private val location: LocationConfigContent,
    private val origin: Origin,
    private val initialUrl: Url,
    private val daemon: ParseDaemon,
    private val tracker: ParseTracker,
) {
    val koog get() = daemon.koog
    val dao get() = daemon.dao
    val log get() = daemon.log

    private val feed get() = tracker.feed

    suspend fun read(): List<RawEvent>? {
        val link = dao.link.readLinkByAlias(initialUrl)
        if (link?.stopsFeed() == true) {
            feed.skipped(PageState.Skipped, "Stopped by its last read: ${link.access}, ${link.content}, ${link.parseOutcome}")
            return null
        }
        val url = link?.let {
            if (link.fetchedAt >= daemon.startedAt) return null
            link.url
        } ?: initialUrl
        val gate = daemon.getRobotGate(origin)
        val fetchMode = dao.origin.readFetchMode(origin.originId) ?: origin.fetchMode
        val fetch = gate.fetchWhenOpen(url, fetchMode).toDataOr {
            daemon.logProblem(it)
            feed.fetchFailed(it)
            return null
        }

        val doc = parseHtmlDocument(fetch.text, fetch.pageUrl).toDataOrNull(daemon::logProblem)
        feed.fetched(fetchMode, fetch, doc)
        if (doc == null) return null
        dao.registerFetch(origin.originId, doc, fetch)

        val feedSchema = origin.getFeedSchema(url, doc).toDataOr {
            daemon.logProblem(it)
            feed.schemaFailed(it)
            return null
        }
        val body = doc.body()
        val pageElements = feedSchema.event?.let { body.tryQuery(it).toDataOrNull(daemon::logProblem) }
        feed.collect(pageElements.orEmpty())
        if (pageElements.isNullOrEmpty()) {
            feed.noEvents(SchemaType.EventFeed)
            return null
        }
        feed.read(SchemaType.EventFeed)

        return pageElements.mapNotNull { element ->
            val feedEvent = RawEvent(
                title = element.queryElement(feedSchema.title).plainText(),
                url = element.queryElement(feedSchema.link).absoluteUrl("href")?.toUrl()?.normalize(),
                image = element.queryElement(feedSchema.image).absoluteUrl("src"),
                descriptionHtml = element.queryElement(feedSchema.description) { it.isPlausibleProse() }.innerHtml(),
                cost = element.queryElement(feedSchema.cost).plainText(),
                date = element.queryElement(feedSchema.date).plainText(),
                startTime = element.queryElement(feedSchema.time).plainText(),
            )

            val pageUrl = feedEvent.url ?: return@mapNotNull feedEvent
            tracker.linkFound()
            val pageLink = dao.link.readLinkByAlias(pageUrl)
            if (pageLink != null && !pageLink.wantsRead()) return@mapNotNull null
            if (!tracker.canReadPage()) {
                tracker.pageDeferred(pageUrl)
                return@mapNotNull null
            }
            val pageEvent = if (tracker.shouldFetch(pageUrl)) {
                val pageOrigin = pageUrl.toOriginId()?.takeIf { it != origin.originId }?.let {
                    dao.origin.readOrCreateOrigin(it)
                } ?: origin
                EventPageReader(location, pageOrigin, pageUrl, daemon, tracker.page(pageUrl)).read()
            } else {
                tracker.pageBenched(pageUrl)
                null
            }

            RawEvent(
                title = pageEvent?.title ?: feedEvent.title,
                url = pageUrl,
                image = pageEvent?.image ?: feedEvent.image,
                descriptionHtml = pageEvent?.descriptionHtml ?: feedEvent.descriptionHtml,
                contact = pageEvent?.contact,
                cost = pageEvent?.cost ?: feedEvent.cost,
                ageMin = pageEvent?.ageMin,
                date = pageEvent?.date ?: feedEvent.date,
                startTime = pageEvent?.startTime ?: feedEvent.startTime,
                endTime = pageEvent?.endTime,
            )
        }
    }

    private suspend fun Origin.getFeedSchema(url: Url, doc: Document): Outcome<EventFeedSchema> {
        val body = doc.body()
        val parsers = dao.parser.read(originId)
        parsers.sortedByDescending { it.lastSuccessAt }.forEach { parser ->
            val schema = parser.schema as? EventFeedSchema ?: return@forEach
            val eventSelector = schema.event ?: return@forEach
            val pageElements = body.tryQuery(eventSelector).toDataOr { return@forEach }
            val isSuccess = !pageElements.isEmpty()
            feed.storedSchemaTried(schema, isSuccess)
            dao.parser.updateResult(parser.parserId, isSuccess)
            if (isSuccess) return Ok(schema)
        }

        if (daemon.lmUsageLimitReached) return LMProblem.UsageLimit

        // td: limit LM call by interval
        val content = koog.readHtml<ContentParse<EventFeedSchema>>(
            url = url,
            doc = doc,
            instructions = SchemaParserText.EventFeedSelectorsInstructions,
            retryCount = lmRetryCount,
            observer = feed,
        ).toDataOr {
            if (it == LMProblem.UsageLimit) {
                daemon.lmUsageLimitReached = true
            }
            return it
        }

        if (content.isIncompleteContent) {
            log.info { "Found incomplete content: $url" }
            dao.origin.registerIncomplete(originId)
        }
        val contentSchema = content.content
        if (!content.isExpectedContent || contentSchema == null) {
            return if (content.isIncompleteContent) SchemaProblem.Incomplete else Problem("Document content was not an event feed")
        }

        val validated = contentSchema.validate(doc.body())
        feed.schemaCreated(contentSchema, validated)
        val schema = validated.toDataOr {
            daemon.logProblem(it)
            return SchemaProblem.Invalid
        }
        dao.parser.create(originId, schema, fetchMode)
        return Ok(schema)
    }
}
