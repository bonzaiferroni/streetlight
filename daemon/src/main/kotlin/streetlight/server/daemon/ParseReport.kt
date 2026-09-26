package streetlight.server.daemon

import kampfire.model.Problem
import kampfire.model.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import streetlight.agent.LMProblem
import streetlight.agent.TrimStats
import streetlight.model.data.OriginId
import java.io.File

/** The build of the parse pipeline, naming the folder its logs are written to. Raised when a stage changes. */
const val parserBuildId = "V3"

val parserLogDir = File("../logs/parser/$parserBuildId")

/** The result of reading one page. */
@Serializable
enum class PageOutcome {
    @SerialName("skipped") Skipped,
    @SerialName("benched") Benched,
    @SerialName("blocked") Blocked,
    @SerialName("unreachable") Unreachable,
    @SerialName("no-html") NoHtml,
    @SerialName("limit") Limit,
    @SerialName("lm-error") LmError,
    @SerialName("read-no-content") NoContent,
    @SerialName("read-invalid-selector") InvalidSelector,
    @SerialName("read-content") Content,
}

/** Maps a problem from fetching a page to its [PageOutcome]. */
fun Problem.toFetchOutcome() = when (this) {
    RobotProblem.Disallowed -> PageOutcome.Blocked
    else -> PageOutcome.Unreachable
}

/** Maps a problem from finding a page schema to its [PageOutcome]. */
fun Problem.toSchemaOutcome() = when (this) {
    LMProblem.UsageLimit -> PageOutcome.Limit
    SchemaProblem.Invalid -> PageOutcome.InvalidSelector
    LMProblem.Busy, LMProblem.Unspecified, LMProblem.Decoding -> PageOutcome.LmError
    else -> PageOutcome.NoContent
}

/** What one check of a location did: its feed, each event page read, and the events the feed yielded. */
@Serializable
class ParseReport(
    val buildId: String,
    val location: String,
    val checkedAt: String,
    val feed: PageReport,
    val links: Int,
    val pages: List<PageReport>,
    val events: EventReport,
) {
    /** Writes this report to `<origin>.json` in [parserLogDir], replacing the report of an earlier check. */
    fun write(originId: OriginId) {
        val file = parserLogDir.resolve("$originId.json")
        file.parentFile.mkdirs()
        file.writeText(reportJson.encodeToString(this))
    }
}

/** One page's pass through each stage, as far as it got. */
@Serializable
class PageReport(val url: String) {
    var outcome = PageOutcome.Skipped
    var status: Int? = null
    var problem: String? = null
    var fetch: FetchReport? = null
    var lm: LmReport? = null
    var schema: SchemaReport? = null
}

/** The fetch of a page, with [textChars] the visible text of the parsed body. */
@Serializable
data class FetchReport(
    val mode: String,
    val finalUrl: String,
    val chars: Int,
    val textChars: Int?,
    val millis: Long,
)

/** The LM call for a page, with the trim of its html and the model's raw response. */
@Serializable
class LmReport {
    var model: String? = null
    var trim: TrimStats? = null
    var capCutChars: Int = 0
    var attempts: Int = 0
    var inputTokens: Int? = null
    var outputTokens: Int? = null
    var millis: Long? = null
    var response: String? = null
}

/**
 * How a page got its schema: a [source] of `stored` or `new`, the stored schemas tried, and for a new one
 * the [validation] result and the fields validation [dropped]. A feed adds its [eventCount] and how many
 * events each field matched in [fieldFill].
 */
@Serializable
class SchemaReport {
    var source: String? = null
    var storedTried = 0
    var validation: String? = null
    var dropped: List<String> = emptyList()
    var eventCount: Int? = null
    var fieldFill: Map<String, Int>? = null
}

/** The events a feed yielded, with the date text of each event whose start could not be parsed. */
@Serializable
class EventReport {
    var found = 0
    var created = 0
    var past = 0
    var duplicates = 0
    var createFailed = 0
    val unparsedDates = mutableListOf<String>()
}

/**
 * Writes [html] to `html/<address>.html` in [parserLogDir], and [promptHtml], the html exactly as the LM
 * read it, beside it as `<address>-trim.html`, replacing any earlier copies.
 */
fun saveHtml(url: Url, html: String, promptHtml: String?) {
    val dir = parserLogDir.resolve("html")
    dir.mkdirs()
    val name = url.toFileName()
    dir.resolve("$name.html").writeText(html)
    promptHtml?.let { dir.resolve("$name-trim.html").writeText(it) }
}

private val reportJson = Json {
    prettyPrint = true
    encodeDefaults = true
}

private fun Url.toFileName() = "${host.orEmpty()}${toRelativePath()}"
    .replace(Regex("[^A-Za-z0-9._-]+"), "_")
    .trim('_')
    .take(200)
