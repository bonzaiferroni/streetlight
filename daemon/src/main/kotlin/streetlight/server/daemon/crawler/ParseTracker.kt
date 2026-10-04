package streetlight.server.daemon.crawler

import kampfire.model.toUrl
import streetlight.model.data.ParseProperty
import streetlight.model.data.RawEntity
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import streetlight.server.daemon.agent.EventClassification
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
import streetlight.model.data.LeadType
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

    fun trackFetchedUrl(url: Url, mode: FetchMode, fetch: FetchText, doc: Document?) = page(url).trackFetched(mode, fetch, doc)

    /** Tracks the fetch of the page at [url] as failed with [problem], striking its origin on a 4xx or 5xx. */
    fun trackFailedFetch(url: Url, problem: Problem) = page(url).trackFailedFetch(problem)

    /**
     * Tracks that no schema could be found for the page at [url], for [problem], striking its origin when the schema
     * was invalid.
     */
    fun trackFailedSchema(url: Url, problem: Problem) {
        page(url).trackFailedSchema(problem)
        if (problem == SchemaProblem.Invalid) url.toOriginId()?.let { schemaStrikes[it] = (schemaStrikes[it] ?: 0) + 1 }
    }

    fun trackNoEvents(url: Url, type: SchemaType) = page(url).trackNoEvents(type)

    fun trackLdValues(url: Url) {
        page(url).notes.add("Declared values from its JSON-LD")
    }

    /** Tracks the page at [url] read in full with a schema of [type], clearing its origin's schema strikes. */
    fun trackReadUrl(url: Url, type: SchemaType) {
        page(url).trackRead(type)
        url.toOriginId()?.let { schemaStrikes.remove(it) }
    }

    fun trackSkippedUrl(url: Url, state: PageState, note: String? = null) = page(url).trackSkipped(state, note)

    fun trackPartialUrl(url: Url, note: String) = page(url).trackPartial(note)

    /** Whether the page at [url] was already read, or tried, this check. */
    fun hasPage(url: Url): Boolean = pages[url]?.isAttempted == true

    fun trackKnownPage() {
        records.known++
    }

    /** Whether another page beyond the lead's own may be read this check. */
    fun canReadPage(): Boolean = pages.count { (url, page) -> url != leadUrl && page.isAttempted } < maxPagesPerCheck

    /**
     * Whether the page at [url] may be fetched, false once its origin is benched: by consecutive failed fetches, or by
     * consecutive schemas that failed validation.
     */
    fun shouldFetch(url: Url): Boolean {
        val originId = url.toOriginId() ?: return true
        return (strikes[originId] ?: 0) < maxStrikes && (schemaStrikes[originId] ?: 0) < maxStrikes
    }

    fun trackFoundLink() {
        links++
    }

    fun trackFoundRecord() {
        records.found++
    }

    /** Tracks an event whose start could not be parsed, marking its lead's page and its own [ParseOutcome.Partial]. */
    fun trackUnparsedEvent(rawEvent: RawEntity) {
        val text = listOfNotNull(rawEvent[ParseProperty.Date], rawEvent[ParseProperty.StartTime]).joinToString(" | ").ifEmpty { "(none)" }
        page(leadUrl).trackPartial()
        (pageOf(rawEvent) ?: page(leadUrl)).trackPartial("Date text did not parse: $text")
    }

    /** Tracks a [rawEntity] found with no name, marking its lead's page and its own [ParseOutcome.Partial]. */
    fun trackUnnamedRecord(rawEntity: RawEntity) {
        records.unnamed++
        page(leadUrl).trackPartial()
        (pageOf(rawEntity) ?: page(leadUrl)).trackPartial("A record had no name")
    }

    fun trackShortenedDescription() {
        records.shortened++
    }

    /**
     * Tracks an event titled [title] dropped since its location named no place and its feed has no location of its
     * own, marking its lead's page and its own [ParseOutcome.Partial].
     */
    fun trackUnlocatedEvent(rawEvent: RawEntity, title: String) {
        val note = "$title had no location: ${rawEvent[ParseProperty.Location] ?: "(none)"}"
        page(leadUrl).trackPartial()
        (pageOf(rawEvent) ?: page(leadUrl)).trackPartial(note)
    }

    /** Tracks the [classification] of [rawEvent] on the page it was read from. */
    fun trackClassifiedEvent(rawEvent: RawEntity, classification: EventClassification) {
        records.classified++
        (pageOf(rawEvent) ?: page(leadUrl)).trackClassification(classification)
    }

    fun trackPastEvent() {
        records.past++
    }

    fun trackDuplicateRecord(rawEntity: RawEntity, name: String, existing: String) {
        records.duplicates++
        noteOn(rawEntity, "$name duplicates $existing")
    }

    fun trackFailedRecord(rawEntity: RawEntity, name: String, problem: Problem) {
        records.createFailed++
        noteOn(rawEntity, "$name could not be created: ${problem.message}")
    }

    fun trackFailedImage(rawEntity: RawEntity, name: String) {
        noteOn(rawEntity, "$name was created without its image")
    }

    fun trackCreatedRecord() {
        records.created++
    }

    fun trackUpdatedRecord() {
        records.updated++
    }

    fun trackSpawnedLocation(rawEvent: RawEntity, text: String, location: Location) {
        records.locationsSpawned++
        noteOn(rawEvent, "Location $text spawned ${location.label}")
    }

    fun trackMatchedLocation(rawEvent: RawEntity, text: String, location: Location) {
        noteOn(rawEvent, "Location $text matched ${location.label}")
    }

    /** Tracks the location [text] of [rawEvent] as naming no distinct place. */
    fun trackFallbackLocation(rawEvent: RawEntity, text: String) {
        noteOn(rawEvent, "Location $text named no distinct place")
    }

    fun trackFailedLocation(rawEvent: RawEntity, text: String, problem: Problem) {
        records.locationsFailed++
        noteOn(rawEvent, "Location $text could not be created: ${problem.message}")
    }

    /**
     * Whether the check needs a report: it failed, some page needs work, its feed was read and yielded no event
     * that was created or already known, a location was spawned or failed to be, or an event was classified.
     */
    fun needsReport(): Boolean = failure != null || allPages().any { it.needsWork } ||
        (page(leadUrl).isAttempted && records.created + records.updated + records.duplicates + records.known == 0) ||
        records.locationsSpawned > 0 || records.locationsFailed > 0 ||
        records.classified > 0

    fun trackFailedCheck(error: Exception) {
        failure = "${error::class.simpleName}: ${error.message}"
    }

    /** The record of each page that reached its server, the lead's own first, for its link. */
    fun records(): List<LinkRecord> = allPages().mapNotNull { it.record() }

    /** Writes the report of the lead of [leadType], and saves the html of the lead's page and of each page that needs work. */
    fun write(leadType: LeadType) {
        report().write(leadType, leadUrl)
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

    /** The tracker of the page [rawEntity] was read from, when it has one of its own. */
    private fun pageOf(rawEntity: RawEntity): PageTracker? = rawEntity[ParseProperty.Url]?.let { pages[it.toUrl()] }

    /** Keeps [note] on the page [rawEntity] was read from, or on the lead's page. */
    private fun noteOn(rawEntity: RawEntity, note: String) {
        (pageOf(rawEntity) ?: page(leadUrl)).notes.add(note)
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

    /** Tracks a page served, whose content is unknown when [doc] is null. */
    internal fun trackFetched(mode: FetchMode, fetch: FetchText, doc: Document?) {
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
     * Tracks a fetch that failed with [problem], striking the page's origin on a 4xx or 5xx, and noting a problem
     * that has no status.
     */
    internal fun trackFailedFetch(problem: Problem) {
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

    override fun schemaCreated(schema: SelectorSchema, validatedSchema: Outcome<SelectorSchema>) {
        val report = schemaReport()
        report.source = "new"
        when (validatedSchema) {
            is Ok -> {
                report.validation = "ok"
                report.dropped = droppedFields(schema, validatedSchema.data)
                this.schema = validatedSchema.data
            }
            is Problem -> report.validation = validatedSchema.message
        }
    }

    /** Tracks that no schema could be found for the page, for [problem]. */
    internal fun trackFailedSchema(problem: Problem) {
        notes.add(problem.message)
        when (problem) {
            SchemaProblem.ScriptingRequired -> {
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

    /** Tracks a page read with a schema of [type] that found no events to build. */
    internal fun trackNoEvents(type: SchemaType) {
        schemaType = type
        page.content = LinkContent.Schema
        page.parseOutcome = ParseOutcome.Fail
        notes.add("The event selector matched nothing")
    }

    /** Tracks a page read in full with a schema of [type]. */
    internal fun trackRead(type: SchemaType) {
        schemaType = type
        page.content = LinkContent.Schema
        page.parseOutcome = ParseOutcome.Complete
    }

    /** Tracks the page as not fetched, for the reason given by [state]. */
    internal fun trackSkipped(state: PageState, note: String? = null) {
        page.state = state
        note?.let { notes.add(it) }
    }

    /** Marks a page read in full as [ParseOutcome.Partial], keeping [note] when given. */
    internal fun trackPartial(note: String? = null) {
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

    internal fun trackClassification(classification: EventClassification) {
        page.classifications.add(classification)
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

/** Tracks the page at [pageUrl] as declaring values, when this map holds any. */
context(tracker: ParseTracker)
fun RawEntity?.trackLdValues(pageUrl: Url) {
    if (!isNullOrEmpty()) tracker.trackLdValues(pageUrl)
}

/** The names of the selectors set in [before] and cleared in [after]. */
private fun droppedFields(before: SelectorSchema, after: SelectorSchema): List<String> {
    val set = Json.encodeToJsonElement(SelectorSchema.serializer(), before).jsonObject
    val kept = Json.encodeToJsonElement(SelectorSchema.serializer(), after).jsonObject
    return set.keys.filter { (kept[it] ?: JsonNull) is JsonNull }
}
