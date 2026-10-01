package streetlight.server.daemon.crawler

import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toHttpProblem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import streetlight.server.daemon.agent.TrimStats
import streetlight.model.data.LinkAccess
import streetlight.model.data.LinkContent
import streetlight.model.data.LeadType
import streetlight.model.data.ParseOutcome
import java.io.File

/** The build of the parse pipeline, naming the folder its reports are written to. */
const val parserBuildId = "V33"

val parserLogDir = File("../logs/parser/$parserBuildId")

/** The state of a page in one check: whether it was attempted, or why not. */
@Serializable
enum class PageState {
    @SerialName("attempted") Attempted,
    @SerialName("skipped") Skipped,
    @SerialName("benched") Benched,
    @SerialName("deferred") Deferred,
}

/** The HTTP status this problem was made from by [toHttpProblem], or `null` when it came from elsewhere. */
fun Problem.toHttpStatus(): Int? = (100..599).firstOrNull { it.toHttpProblem() == this }

/** The report of one check of a lead: its own page, each page it led to, and the records it found. */
@Serializable
class ParseReport(
    val buildId: String,
    val source: String,
    val checkedAt: String,
    val lead: PageReport,
    val links: Int,
    val pages: List<PageReport>,
    val records: RecordReport,
    val failure: String? = null,
) {
    /**
     * Writes this report to `<type>-<address>.json` in [parserLogDir], named for the [leadType] and url of its lead,
     * replacing the report of an earlier check of it.
     */
    fun write(leadType: LeadType, leadUrl: Url) {
        val file = parserLogDir.resolve("$leadType-${leadUrl.toFileName()}.json")
        file.parentFile.mkdirs()
        file.writeText(reportJson.encodeToString(this))
    }
}

/** One page's pass through each stage, as far as it got. */
@Serializable
class PageReport(val url: String) {
    var state = PageState.Skipped
    var access: LinkAccess? = null
    var content: LinkContent? = null
    var parseOutcome: ParseOutcome? = null
    var status: Int? = null
    var notes: List<String> = emptyList()
    var fetch: FetchReport? = null
    var trim: TrimStats? = null
    val lm = mutableListOf<LmReport>()
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

/** One LM request for a page, of [kind] `schema` or `time`, with the trim of its html and the raw response. */
@Serializable
class LmReport(val kind: String? = null) {
    var model: String? = null
    var capCutChars: Int = 0
    var attempts: Int = 0
    var inputTokens: Int? = null
    var outputTokens: Int? = null
    var millis: Long? = null
    var response: String? = null
}

/**
 * The schema stage of a page: a [source] of `stored` or `new`, the stored schemas tried, and for a new one
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

/** The counts of the records a check found and what became of them; the story of each is in its page's notes. */
@Serializable
class RecordReport {
    var found = 0
    var created = 0
    var updated = 0
    var past = 0
    var unnamed = 0
    var shortened = 0
    var duplicates = 0
    var known = 0
    var createFailed = 0
    var locationsSpawned = 0
    var locationsFailed = 0
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
