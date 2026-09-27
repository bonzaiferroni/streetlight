package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import streetlight.agent.FetchText
import streetlight.agent.SchemaObserver
import streetlight.agent.SchemaProblem
import streetlight.agent.fieldFill
import streetlight.agent.LMProblem
import streetlight.agent.TrimResult
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.FetchMode
import streetlight.model.data.Location
import streetlight.model.data.LinkAccess
import streetlight.model.data.LinkContent
import streetlight.model.data.OriginId
import streetlight.model.data.ParseOutcome
import streetlight.model.data.SchemaType
import streetlight.model.data.SelectorSchema
import streetlight.model.data.toOriginId
import kotlin.time.Clock

/** Tracks one check of a feed source, reported to by its readers and consulted before each fetch. */
class ParseTracker(private val source: String, feedUrl: Url) {
    private val checkedAt = Clock.System.now()
    private val pages = mutableListOf<PageTracker>()
    private val strikes = mutableMapOf<OriginId, Int>()
    private val events = EventReport()
    private var links = 0
    private var pagesRead = 0
    private var failure: String? = null

    /** The tracker of the feed page itself. */
    val feed = PageTracker(feedUrl, this)

    /** A tracker for the event page at [url], about to be read, counted toward the check's limit of pages. */
    fun page(url: Url) = PageTracker(url, this).also {
        pages.add(it)
        pagesRead++
    }

    /** Whether the event page at [url] was already read, or tried, this check. */
    fun hasPage(url: Url): Boolean = pages.any { it.url == url && it.isAttempted }

    /** Records an event page skipped because its link already has an outcome. */
    fun pageKnown() {
        events.known++
    }

    /** Whether another event page may be read this check. */
    fun canReadPage(): Boolean = pagesRead < maxPagesPerCheck

    /** Records the event page at [url] as not read, since its origin is benched. */
    fun pageBenched(url: Url) {
        pages.add(PageTracker(url, this).apply { skipped(PageState.Benched) })
    }

    /** Records the event page at [url] as not read, since the check reached its limit of pages. */
    fun pageDeferred(url: Url) {
        pages.add(PageTracker(url, this).apply { skipped(PageState.Deferred) })
    }

    /** Whether the page at [url] may be fetched, false once its origin is benched. */
    fun shouldFetch(url: Url): Boolean = (url.toOriginId()?.let { strikes[it] } ?: 0) < maxStrikes

    fun linkFound() {
        links++
    }

    fun eventFound() {
        events.found++
    }

    /** Records an event whose start could not be parsed, marking its feed and page [ParseOutcome.Partial]. */
    fun eventUnparsed(event: RawEvent) {
        val text = listOfNotNull(event.date, event.startTime).joinToString(" | ").ifEmpty { "(none)" }
        events.unparsedDates.add(text)
        feed.partial("Date text did not parse: $text")
        pages.firstOrNull { it.url == event.url }?.partial("Date text did not parse: $text")
    }

    /** Records an event found with no title, marking its feed and page [ParseOutcome.Partial]. */
    fun eventUntitled(event: RawEvent) {
        events.untitled++
        feed.partial("An event had no title")
        pages.firstOrNull { it.url == event.url }?.partial("The event had no title")
    }

    fun descriptionShortened() {
        events.shortened++
    }

    /**
     * Records an event titled [title] dropped since its location [text] named no place and its feed has no location
     * of its own, marking its feed and page [ParseOutcome.Partial].
     */
    fun eventUnlocated(event: RawEvent, title: String) {
        events.unlocatedEvents.add("$title: ${event.location ?: "(none)"}")
        feed.partial("An event had no location")
        pages.firstOrNull { it.url == event.url }?.partial("The event had no location")
    }

    fun eventPast() {
        events.past++
    }

    /** Records an event whose [title] fuzzily matched the [existing] title of an event the same day. */
    fun eventDuplicate(title: String, existing: String) {
        events.duplicates++
        events.duplicateTitles.add("$title → $existing")
    }

    /** Records an event titled [title] that could not be created, for [problem]. */
    fun eventFailed(title: String, problem: Problem) {
        events.createFailed++
        events.createFailures.add("$title: ${problem.message}")
    }

    /** Records an event titled [title] created without its image, which could not be stored. */
    fun imageFailed(title: String) {
        events.imageFailures.add(title)
    }

    fun eventCreated() {
        events.created++
    }

    /** Records the location [text] as naming the new [location]. */
    fun locationSpawned(text: String, location: Location) {
        events.spawnedLocations.add("$text → ${location.label}")
    }

    /** Records the location [text] as naming the known [location]. */
    fun locationMatched(text: String, location: Location) {
        events.matchedLocations.add("$text → ${location.label}")
    }

    /** Records the location [text] as naming no distinct place. */
    fun locationFellBack(text: String) {
        events.fallbackLocations.add(text)
    }

    /** Records the location [text] as naming a place that could not be created, for [problem]. */
    fun locationFailed(text: String, problem: Problem) {
        events.locationFailures.add("$text: ${problem.message}")
    }

    /**
     * Whether the check needs a report: it failed, some page needs work, its feed was read and yielded no event
     * that was created or already known, or a location was spawned or failed to be.
     */
    fun needsReport(): Boolean = failure != null || allPages().any { it.needsWork } ||
        (feed.isAttempted && events.created + events.duplicates + events.known == 0) ||
        events.spawnedLocations.isNotEmpty() || events.locationFailures.isNotEmpty()

    /** Records the check as cut short by [error]. */
    fun failed(error: Exception) {
        failure = "${error::class.simpleName}: ${error.message}"
    }

    /** The record of each page that reached its server, the feed first, for its link. */
    fun records(): List<LinkRecord> = allPages().mapNotNull { it.record() }

    /** Writes the report to `<origin>.json`, and saves the html of the feed and of each page that needs work. */
    fun write(originId: OriginId) {
        report().write(originId)
        (listOf(feed) + pages.filter { it.needsWork }).forEach { it.saveHtml() }
    }

    /** Builds the report of the check. */
    fun report() = ParseReport(
        buildId = parserBuildId,
        source = source,
        checkedAt = checkedAt.toString(),
        feed = feed.report(),
        links = links,
        pages = pages.map { it.report() },
        events = events,
        failure = failure,
    )

    internal fun strike(url: Url) {
        val originId = url.toOriginId() ?: return
        strikes[originId] = (strikes[originId] ?: 0) + 1
    }

    internal fun clearStrikes(url: Url) {
        url.toOriginId()?.let { strikes.remove(it) }
    }

    private fun allPages() = listOf(feed) + pages
}

/** Tracks one page's pass through each stage, and observes its LM call. */
class PageTracker internal constructor(internal val url: Url, private val tracker: ParseTracker) : SchemaObserver {
    private val page = PageReport(url.value)
    private val items = mutableListOf<Any>()
    private val notes = mutableListOf<String>()
    private var fetch: FetchText? = null
    private var fetchMode: FetchMode? = null
    private var promptHtml: String? = null
    private var schema: SelectorSchema? = null
    private var schemaType: SchemaType? = null

    /** Keeps [item] to draw on when the report is built, such as the feed's event elements. */
    fun collect(item: Any) {
        items.add(item)
    }

    /** Records a page served, whose content is unknown when [doc] is null. */
    fun fetched(mode: FetchMode, fetch: FetchText, doc: Document?) {
        this.fetch = fetch
        fetchMode = mode
        tracker.clearStrikes(url)
        page.state = PageState.Attempted
        page.access = LinkAccess.Granted
        page.status = 200
        page.fetch = FetchReport(
            mode = mode.name,
            finalUrl = fetch.pageUrl.value,
            chars = fetch.text.length,
            textChars = doc?.body()?.text()?.length,
            millis = fetch.millis,
        )
        if (doc == null) {
            page.content = LinkContent.Unknown
            notes.add("Not html")
        }
    }

    /** Records a fetch that failed with [problem], striking the page's origin on a 4xx or 5xx. */
    fun fetchFailed(problem: Problem) {
        page.state = PageState.Attempted
        notes.add(problem.message)
        if (problem == RobotProblem.Disallowed) {
            page.access = LinkAccess.RobotsBlock
            return
        }
        val status = problem.toHttpStatus() ?: return
        page.status = status
        if (status >= 400) tracker.strike(url)
        if (status !in 500..599) page.access = LinkAccess.Refused
    }

    override fun requested(kind: String) {
        page.lm.add(LmReport(kind))
    }

    override fun storedSchemaTried(schema: SelectorSchema, chosen: Boolean) {
        val report = schemaReport()
        report.storedTried++
        if (chosen) {
            report.source = "stored"
            this.schema = schema
        }
    }

    override fun schemaCreated(schema: SelectorSchema, validated: Outcome<SelectorSchema>) {
        val report = schemaReport()
        report.source = "new"
        when (validated) {
            is Ok -> {
                report.validation = "ok"
                report.dropped = droppedFields(schema, validated.data)
                this.schema = validated.data
            }
            is Problem -> report.validation = validated.message
        }
    }

    /** Records that no schema could be found for the page, for [problem]. */
    fun schemaFailed(problem: Problem) {
        notes.add(problem.message)
        when (problem) {
            SchemaProblem.Incomplete -> {
                page.content = LinkContent.Unread
                if (fetchMode == FetchMode.Scripting) page.parseOutcome = ParseOutcome.Fail
            }
            SchemaProblem.Invalid -> {
                page.content = LinkContent.Schema
                page.parseOutcome = ParseOutcome.Fail
                page.schema?.validation?.let { notes.add(it) }
            }
            LMProblem.UsageLimit, LMProblem.Busy, LMProblem.Unspecified, LMProblem.Decoding -> Unit
            else -> page.content = LinkContent.OffSchema
        }
    }

    /** Records a page read with a schema of [type] that found no events to build. */
    fun noEvents(type: SchemaType) {
        schemaType = type
        page.content = LinkContent.Schema
        page.parseOutcome = ParseOutcome.Fail
        notes.add("The event selector matched nothing")
    }

    /** Records a page read in full with a schema of [type]. */
    fun read(type: SchemaType) {
        schemaType = type
        page.content = LinkContent.Schema
        page.parseOutcome = ParseOutcome.Complete
    }

    /** Records the page as not fetched, for the reason given by [state]. */
    internal fun skipped(state: PageState, note: String? = null) {
        page.state = state
        note?.let { notes.add(it) }
    }

    /** Marks a page read in full as [ParseOutcome.Partial], keeping [note]. */
    internal fun partial(note: String) {
        if (page.parseOutcome == ParseOutcome.Complete) page.parseOutcome = ParseOutcome.Partial
        notes.add(note)
    }

    internal val isAttempted get() = page.state == PageState.Attempted

    /** Whether the page needs work: a partial or failed parse, or content no schema reads. */
    internal val needsWork get() = page.parseOutcome in needsWorkOutcomes || page.content in needsWorkContent

    /** The record for the page's link, or `null` when the page never reached its server. */
    internal fun record(): LinkRecord? = page.access?.let { access ->
        LinkRecord(url, access, page.content, schemaType, page.parseOutcome, notes.distinct().joinToString("; ").ifEmpty { null })
    }

    /** Saves the page's html with the html the LM read. */
    internal fun saveHtml() {
        fetch?.let { saveHtml(it.pageUrl, it.text, promptHtml) }
    }

    override fun trimmed(result: TrimResult, promptHtml: String) {
        this.promptHtml = promptHtml
        lmReport().apply {
            trim = result.stats
            capCutChars = result.html.length - promptHtml.length
        }
    }

    override fun responded(model: String, json: String, inputTokens: Int?, outputTokens: Int?, millis: Long, attempts: Int) {
        lmReport().apply {
            this.model = model
            this.response = json
            this.inputTokens = inputTokens
            this.outputTokens = outputTokens
            this.millis = millis
            this.attempts = attempts
        }
    }

    internal fun report(): PageReport {
        val feedSchema = schema as? EventFeedSchema
        val events = items.filterIsInstance<List<*>>().lastOrNull()?.filterIsInstance<Element>()
        if (feedSchema != null && events != null) {
            schemaReport().eventCount = events.size
            schemaReport().fieldFill = feedSchema.fieldFill(events)
        }
        page.notes = notes.distinct()
        return page
    }

    private fun schemaReport() = page.schema ?: SchemaReport().also { page.schema = it }

    private fun lmReport() = page.lm.lastOrNull() ?: LmReport().also { page.lm.add(it) }
}

/** The access, content, schema type, parse outcome and note of the page at [url], to record on its link. */
data class LinkRecord(
    val url: Url,
    val access: LinkAccess,
    val content: LinkContent?,
    val schemaType: SchemaType?,
    val parseOutcome: ParseOutcome?,
    val note: String?,
)

private const val maxStrikes = 3
private const val maxPagesPerCheck = 30

private val needsWorkOutcomes = setOf(ParseOutcome.Partial, ParseOutcome.Fail)

private val needsWorkContent = setOf(LinkContent.OffSchema, LinkContent.OffScope, LinkContent.Unknown, LinkContent.Unread)

/** The names of the selectors set in [before] and cleared in [after]. */
private fun droppedFields(before: SelectorSchema, after: SelectorSchema): List<String> {
    val set = Json.encodeToJsonElement(SelectorSchema.serializer(), before).jsonObject
    val kept = Json.encodeToJsonElement(SelectorSchema.serializer(), after).jsonObject
    return set.keys.filter { (kept[it] ?: JsonNull) is JsonNull }
}
