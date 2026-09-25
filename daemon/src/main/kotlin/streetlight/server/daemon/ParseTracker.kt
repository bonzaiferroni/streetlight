package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toHttpProblem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import streetlight.agent.FetchText
import streetlight.agent.HtmlParseObserver
import streetlight.agent.TrimResult
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.FetchMode
import streetlight.model.data.OriginId
import streetlight.model.data.SelectorSchema
import streetlight.model.data.toOriginId
import kotlin.time.Clock

/**
 * Tracks one check of a location: the readers report raw objects to it as they go, and consult it before
 * fetching. [report] builds the [ParseReport] once the check is finished.
 *
 * Each 4xx or 5xx response adds a strike to the origin of the page requested, and a successful fetch from
 * that origin clears them. An origin with [maxStrikes] consecutive strikes is benched for the rest of the check.
 */
class ParseTracker(private val location: String, feedUrl: Url) {
    private val checkedAt = Clock.System.now()
    private val pages = mutableListOf<PageTracker>()
    private val strikes = mutableMapOf<OriginId, Int>()
    private val events = EventReport()
    private var links = 0

    /** The tracker of the feed page itself. */
    val feed = PageTracker(feedUrl, this)

    /** A tracker for the event page at [url], linked from the feed. */
    fun page(url: Url) = PageTracker(url, this).also { pages.add(it) }

    /** Whether the page at [url] may be fetched, false once its origin is benched. */
    fun shouldFetch(url: Url): Boolean = (url.toOriginId()?.let { strikes[it] } ?: 0) < maxStrikes

    fun linkFound() {
        links++
    }

    fun eventFound() {
        events.found++
    }

    /** Records an event dropped because the start of [event] could not be parsed from its date text. */
    fun eventUnparsed(event: RawEvent) {
        events.unparsedDates.add(listOfNotNull(event.date, event.startTime).joinToString(" | ").ifEmpty { "(none)" })
    }

    fun eventDuplicate() {
        events.duplicates++
    }

    fun eventFailed() {
        events.createFailed++
    }

    fun eventCreated() {
        events.created++
    }

    /** Builds the report of the check. */
    fun report() = ParseReport(
        buildId = parserBuildId,
        location = location,
        checkedAt = checkedAt.toString(),
        feed = feed.report(),
        links = links,
        pages = pages.map { it.report() },
        events = events,
    )

    internal fun strike(url: Url) {
        val originId = url.toOriginId() ?: return
        strikes[originId] = (strikes[originId] ?: 0) + 1
    }

    internal fun clearStrikes(url: Url) {
        url.toOriginId()?.let { strikes.remove(it) }
    }
}

/** Tracks one page's pass through each stage, and observes its LM call. */
class PageTracker internal constructor(private val url: Url, private val tracker: ParseTracker) : HtmlParseObserver {
    private val page = PageReport(url.value)
    private val items = mutableListOf<Any>()
    private var fetch: FetchText? = null
    private var promptHtml: String? = null
    private var schema: SelectorSchema? = null

    /** Keeps [item] to draw on when the report is built, such as the feed's event elements. */
    fun collect(item: Any) {
        items.add(item)
    }

    fun fetched(mode: FetchMode, fetch: FetchText, doc: Document?) {
        this.fetch = fetch
        tracker.clearStrikes(url)
        page.status = 200
        page.fetch = FetchReport(
            mode = mode.name,
            finalUrl = fetch.pageUrl.value,
            chars = fetch.text.length,
            textChars = doc?.body()?.text()?.length,
            millis = fetch.millis,
        )
    }

    /** Records a stored [schema] tried on the page, and whether it was [chosen]. */
    fun storedSchemaTried(schema: SelectorSchema, chosen: Boolean) {
        val report = schemaReport()
        report.storedTried++
        if (chosen) {
            report.source = "stored"
            this.schema = schema
        }
    }

    /** Records a [schema] from the LM and the outcome of its validation against the page. */
    fun schemaCreated(schema: SelectorSchema, validated: Outcome<SelectorSchema>) {
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

    /** Records the page's [outcome], striking its origin on a 4xx or 5xx, and saves its html with the html the LM read. */
    fun finished(outcome: PageOutcome, problem: Problem? = null) {
        page.outcome = outcome
        page.problem = problem?.message
        problem?.toHttpStatus()?.let { status ->
            page.status = status
            if (status >= 400) tracker.strike(url)
        }
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
        return page
    }

    private fun schemaReport() = page.schema ?: SchemaReport().also { page.schema = it }

    private fun lmReport() = page.lm ?: LmReport().also { page.lm = it }
}

private const val maxStrikes = 3

/** The HTTP status this problem was made from by [toHttpProblem], or `null` when it came from elsewhere. */
private fun Problem.toHttpStatus(): Int? = (100..599).firstOrNull { it.toHttpProblem() == this }

/** The names of the selectors set in [before] and cleared in [after]. */
private fun droppedFields(before: SelectorSchema, after: SelectorSchema): List<String> {
    val set = Json.encodeToJsonElement(SelectorSchema.serializer(), before).jsonObject
    val kept = Json.encodeToJsonElement(SelectorSchema.serializer(), after).jsonObject
    return set.keys.filter { (kept[it] ?: JsonNull) is JsonNull }
}
