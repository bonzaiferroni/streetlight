package streetlight.server.daemon.crawler

import kampfire.model.toUrl
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import streetlight.server.daemon.agent.FetchText
import streetlight.server.daemon.agent.SchemaObserver
import streetlight.server.daemon.agent.SchemaProblem
import streetlight.server.daemon.agent.fieldFill
import streetlight.server.daemon.agent.LMProblem
import streetlight.server.daemon.agent.TrimResult
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

/**
 * Tracks one check of a lead, reported to by the crawl and consulted before each fetch. Each page of the check is
 * tracked by its url: the lead's own page, and any page the lead leads to.
 */
class ParseTracker(private val source: String, private val leadUrl: Url) {
    private val checkedAt = Clock.System.now()
    private val pages = mutableMapOf(leadUrl to PageTracker(leadUrl, this))
    private val strikes = mutableMapOf<OriginId, Int>()
    private val schemaStrikes = mutableMapOf<OriginId, Int>()
    private val records = RecordReport()
    private var links = 0
    private var failure: String? = null

    /** The tracker of the page at [url], created when it is first tracked; it observes the page's LM calls. */
    fun page(url: Url): PageTracker = pages.getOrPut(url) { PageTracker(url, this) }

    /** Keeps [item] for the page at [url], to draw on when the report is built, such as the feed's event elements. */
    fun collect(url: Url, item: Any) = page(url).collect(item)

    /** Records the page at [url] served, whose content is unknown when [doc] is null. */
    fun fetched(url: Url, mode: FetchMode, fetch: FetchText, doc: Document?) = page(url).fetched(mode, fetch, doc)

    /** Records the fetch of the page at [url] as failed with [problem], striking its origin on a 4xx or 5xx. */
    fun fetchFailed(url: Url, problem: Problem) = page(url).fetchFailed(problem)

    /**
     * Records that no schema could be found for the page at [url], for [problem], striking its origin when the schema
     * was invalid.
     */
    fun schemaFailed(url: Url, problem: Problem) {
        page(url).schemaFailed(problem)
        if (problem == SchemaProblem.Invalid) url.toOriginId()?.let { schemaStrikes[it] = (schemaStrikes[it] ?: 0) + 1 }
    }

    /** Records the page at [url] read with a schema of [type] that found no events to build. */
    fun noEvents(url: Url, type: SchemaType) = page(url).noEvents(type)

    /** Records the page at [url] as read from the structured data it declares, without the LM. */
    fun readStructured(url: Url) {
        page(url).notes.add("Read from its JSON-LD")
    }

    /** Records the page at [url] read in full with a schema of [type], clearing its origin's schema strikes. */
    fun read(url: Url, type: SchemaType) {
        page(url).read(type)
        url.toOriginId()?.let { schemaStrikes.remove(it) }
    }

    /** Records the page at [url] as not fetched, for the reason given by [state]. */
    fun skipped(url: Url, state: PageState, note: String? = null) = page(url).skipped(state, note)

    /** Marks the page at [url], read in full, as [ParseOutcome.Partial], keeping [note]. */
    fun partial(url: Url, note: String) = page(url).partial(note)

    /** Whether the page at [url] was already read, or tried, this check. */
    fun hasPage(url: Url): Boolean = pages[url]?.isAttempted == true

    /** Records an event page skipped because its link already has an outcome. */
    fun pageKnown() {
        records.known++
    }

    /** Whether another page beyond the lead's own may be read this check. */
    fun canReadPage(): Boolean = pages.count { (url, page) -> url != leadUrl && page.isAttempted } < maxPagesPerCheck

    /** Records the event page at [url] as not read, since its origin is benched. */
    fun pageBenched(url: Url) = skipped(url, PageState.Benched)

    /** Records the event page at [url] as not read, since the check reached its limit of pages. */
    fun pageDeferred(url: Url) = skipped(url, PageState.Deferred)

    /**
     * Whether the page at [url] may be fetched, false once its origin is benched: by consecutive failed fetches, or by
     * consecutive schemas that failed validation.
     */
    fun shouldFetch(url: Url): Boolean {
        val originId = url.toOriginId() ?: return true
        return (strikes[originId] ?: 0) < maxStrikes && (schemaStrikes[originId] ?: 0) < maxStrikes
    }

    fun linkFound() {
        links++
    }

    fun recordFound() {
        records.found++
    }

    /** Records an event whose start could not be parsed, marking its lead's page and its own [ParseOutcome.Partial]. */
    fun eventUnparsed(event: PropertyMap) {
        val text = listOfNotNull(event[ParseProperty.Date], event[ParseProperty.StartTime]).joinToString(" | ").ifEmpty { "(none)" }
        page(leadUrl).partial()
        (pageOf(event) ?: page(leadUrl)).partial("Date text did not parse: $text")
    }

    /** Records a [record] found with no name, marking its lead's page and its own [ParseOutcome.Partial]. */
    fun recordUnnamed(record: PropertyMap) {
        records.unnamed++
        page(leadUrl).partial()
        (pageOf(record) ?: page(leadUrl)).partial("A record had no name")
    }

    fun descriptionShortened() {
        records.shortened++
    }

    /**
     * Records an event titled [title] dropped since its location named no place and its feed has no location of its
     * own, marking its lead's page and its own [ParseOutcome.Partial].
     */
    fun eventUnlocated(event: PropertyMap, title: String) {
        val note = "$title had no location: ${event[ParseProperty.Location] ?: "(none)"}"
        page(leadUrl).partial()
        (pageOf(event) ?: page(leadUrl)).partial(note)
    }

    fun eventPast() {
        records.past++
    }

    /** Records a [record] named [name] that duplicates the record already stored as [existing]. */
    fun recordDuplicate(record: PropertyMap, name: String, existing: String) {
        records.duplicates++
        noteOn(record, "$name duplicates $existing")
    }

    /** Records a [record] named [name] that could not be created, for [problem]. */
    fun recordFailed(record: PropertyMap, name: String, problem: Problem) {
        records.createFailed++
        noteOn(record, "$name could not be created: ${problem.message}")
    }

    /** Records a [record] named [name] created without its image, which could not be stored. */
    fun imageFailed(record: PropertyMap, name: String) {
        noteOn(record, "$name was created without its image")
    }

    fun recordCreated() {
        records.created++
    }

    /** Records the location [text] of [event] as naming the new [location]. */
    fun locationSpawned(event: PropertyMap, text: String, location: Location) {
        records.locationsSpawned++
        noteOn(event, "Location $text spawned ${location.label}")
    }

    /** Records the location [text] of [event] as naming the known [location]. */
    fun locationMatched(event: PropertyMap, text: String, location: Location) {
        noteOn(event, "Location $text matched ${location.label}")
    }

    /** Records the location [text] of [event] as naming no distinct place. */
    fun locationFellBack(event: PropertyMap, text: String) {
        noteOn(event, "Location $text named no distinct place")
    }

    /** Records the location [text] of [event] as naming a place that could not be created, for [problem]. */
    fun locationFailed(event: PropertyMap, text: String, problem: Problem) {
        records.locationsFailed++
        noteOn(event, "Location $text could not be created: ${problem.message}")
    }

    /**
     * Whether the check needs a report: it failed, some page needs work, its feed was read and yielded no event
     * that was created or already known, or a location was spawned or failed to be.
     */
    fun needsReport(): Boolean = failure != null || allPages().any { it.needsWork } ||
        (page(leadUrl).isAttempted && records.created + records.duplicates + records.known == 0) ||
        records.locationsSpawned > 0 || records.locationsFailed > 0

    /** Records the check as cut short by [error]. */
    fun failed(error: Exception) {
        failure = "${error::class.simpleName}: ${error.message}"
    }

    /** The record of each page that reached its server, the lead's own first, for its link. */
    fun records(): List<LinkRecord> = allPages().mapNotNull { it.record() }

    /** Writes the report to `<origin>.json`, and saves the html of the lead's page and of each page that needs work. */
    fun write(originId: OriginId) {
        report().write(originId)
        allPages().filter { it.url == leadUrl || it.needsWork }.forEach { it.saveHtml() }
    }

    /** Builds the report of the check. */
    fun report() = ParseReport(
        buildId = parserBuildId,
        source = source,
        checkedAt = checkedAt.toString(),
        lead = page(leadUrl).report(),
        links = links,
        pages = pages.filterKeys { it != leadUrl }.values.map { it.report() },
        records = records,
        failure = failure,
    )

    internal fun strike(url: Url) {
        val originId = url.toOriginId() ?: return
        strikes[originId] = (strikes[originId] ?: 0) + 1
    }

    internal fun clearStrikes(url: Url) {
        url.toOriginId()?.let { strikes.remove(it) }
    }

    private fun allPages() = listOf(page(leadUrl)) + pages.filterKeys { it != leadUrl }.values

    /** The tracker of the page [record] was read from, when it has one of its own. */
    private fun pageOf(record: PropertyMap): PageTracker? = record[ParseProperty.Url]?.let { pages[it.toUrl()] }

    /** Keeps [note] on the page [record] was read from, or on the lead's page. */
    private fun noteOn(record: PropertyMap, note: String) {
        (pageOf(record) ?: page(leadUrl)).notes.add(note)
    }
}

/** Tracks one page's pass through each stage, and observes its LM calls; reported to through its [ParseTracker]. */
class PageTracker internal constructor(internal val url: Url, private val tracker: ParseTracker) : SchemaObserver {
    private val page = PageReport(url.value)
    private val items = mutableListOf<Any>()
    internal val notes = mutableListOf<String>()
    private var fetch: FetchText? = null
    private var fetchMode: FetchMode? = null
    private var promptHtml: String? = null
    private var schema: SelectorSchema? = null
    private var schemaType: SchemaType? = null

    /** Keeps [item] to draw on when the report is built, such as the feed's event elements. */
    internal fun collect(item: Any) {
        items.add(item)
    }

    /** Records a page served, whose content is unknown when [doc] is null. */
    internal fun fetched(mode: FetchMode, fetch: FetchText, doc: Document?) {
        this.fetch = fetch
        fetchMode = mode
        tracker.clearStrikes(url)
        page.state = PageState.Attempted
        page.access = LinkAccess.Granted
        page.status = 200
        page.fetch = FetchReport(
            mode = mode.name,
            finalUrl = fetch.servedUrl.value,
            chars = fetch.text.length,
            textChars = doc?.body()?.text()?.length,
            millis = fetch.millis,
        )
        if (doc == null) {
            page.content = LinkContent.Unknown
            notes.add("Not html")
        }
    }

    /**
     * Records a fetch that failed with [problem], striking the page's origin on a 4xx or 5xx, and noting a problem
     * that has no status.
     */
    internal fun fetchFailed(problem: Problem) {
        page.state = PageState.Attempted
        if (problem == RobotProblem.Disallowed) {
            page.access = LinkAccess.RobotsBlock
            return
        }
        val status = problem.toHttpStatus()
        if (status == null) {
            notes.add(problem.message)
            return
        }
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
    internal fun schemaFailed(problem: Problem) {
        notes.add(problem.message)
        when (problem) {
            SchemaProblem.Incomplete -> {
                page.content = LinkContent.Unread
                if (fetchMode == FetchMode.Scripting) page.parseOutcome = ParseOutcome.Fail
            }
            SchemaProblem.Invalid -> {
                page.content = LinkContent.Schema
                page.parseOutcome = ParseOutcome.Fail
            }
            LMProblem.UsageLimit, LMProblem.Busy, LMProblem.Unspecified, LMProblem.Decoding -> Unit
            else -> page.content = LinkContent.OffSchema
        }
    }

    /** Records a page read with a schema of [type] that found no events to build. */
    internal fun noEvents(type: SchemaType) {
        schemaType = type
        page.content = LinkContent.Schema
        page.parseOutcome = ParseOutcome.Fail
        notes.add("The event selector matched nothing")
    }

    /** Records a page read in full with a schema of [type]. */
    internal fun read(type: SchemaType) {
        schemaType = type
        page.content = LinkContent.Schema
        page.parseOutcome = ParseOutcome.Complete
    }

    /** Records the page as not fetched, for the reason given by [state]. */
    internal fun skipped(state: PageState, note: String? = null) {
        page.state = state
        note?.let { notes.add(it) }
    }

    /** Marks a page read in full as [ParseOutcome.Partial], keeping [note] when given. */
    internal fun partial(note: String? = null) {
        if (page.parseOutcome == ParseOutcome.Complete) page.parseOutcome = ParseOutcome.Partial
        note?.let { notes.add(it) }
    }

    internal val isAttempted get() = page.state == PageState.Attempted

    /** Whether the page needs work: a partial or failed parse, or content no schema reads. */
    internal val needsWork get() = page.parseOutcome in needsWorkOutcomes || page.content in needsWorkContent

    /** The record for the page's link, or `null` when the page never reached its server. */
    internal fun record(): LinkRecord? = page.access?.let { access ->
        LinkRecord(url, access, page.content, schemaType, page.parseOutcome)
    }

    /** Saves the page's html with the html the LM read. */
    internal fun saveHtml() {
        fetch?.let { saveHtml(it.servedUrl, it.text, promptHtml) }
    }

    override fun trimmed(result: TrimResult, promptHtml: String) {
        this.promptHtml = promptHtml
        page.trim = result.stats
        lmReport().capCutChars = result.html.length - promptHtml.length
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

/** The access, content, schema type and parse outcome of the page at [url], to record on its link. */
data class LinkRecord(
    val url: Url,
    val access: LinkAccess,
    val content: LinkContent?,
    val schemaType: SchemaType?,
    val parseOutcome: ParseOutcome?,
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
