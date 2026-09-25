package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Problem
import kampfire.model.Url
import streetlight.agent.HtmlTrimmer
import streetlight.agent.LMProblem
import java.io.File
import kotlin.time.Instant

/** The result of reading one page, named by [folder] in the log and in the html cache. */
enum class PageOutcome(val folder: String) {
    Skipped("skipped"),
    Blocked("blocked"),
    Unreachable("unreachable"),
    NoHtml("no-html"),
    Limit("limit"),
    LmError("lm-error"),
    NoContent("read-no-content"),
    InvalidSelector("read-invalid-selector"),
    Content("read-content"),
}

/** Maps a problem from fetching a page to its [PageOutcome]. */
fun Problem.toFetchOutcome() = when (this) {
    RobotProblem.Disallowed -> PageOutcome.Blocked
    else -> PageOutcome.Unreachable
}

/** Maps a problem from finding a page schema to its [PageOutcome]. */
fun Problem.toSchemaOutcome() = when (this) {
    LMProblem.UsageLimit -> PageOutcome.Limit
    LMProblem.Busy, LMProblem.Unspecified, LMProblem.Decoding -> PageOutcome.LmError
    else -> PageOutcome.NoContent
}

/** Tallies the read of one feed at [url] and the event pages it links to. */
class FeedReport(val url: Url) {
    var outcome = PageOutcome.Skipped
    var links = 0
    var newEvents = 0
    val pages = mutableListOf<PageOutcome>()
}

/** Tallies one pass of the daemon over its checkable locations. */
class CheckReport(val startedAt: Instant) {
    val feeds = mutableListOf<FeedReport>()

    /** Renders the check summary followed by a summary of each feed. */
    fun render(finishedAt: Instant) = buildString {
        val pages = feeds.flatMap { it.pages }
        appendLine("Check: $startedAt to $finishedAt")
        appendLine("Feeds: ${feeds.map { it.outcome }.tally()}")
        appendLine("Links: ${feeds.sumOf { it.links }}")
        appendLine("Event pages: ${pages.tally()}")
        appendLine("New events: ${feeds.sumOf { it.newEvents }}")
        feeds.forEach { feed ->
            appendLine()
            appendLine("${feed.url} [${feed.outcome.folder}]")
            appendLine("    Links: ${feed.links}")
            appendLine("    Event pages: ${feed.pages.tally()}")
            appendLine("    New events: ${feed.newEvents}")
        }
    }

    /** Writes the rendered report to `<timestamp>.log` in [parserLogDir]. */
    fun write(finishedAt: Instant) {
        val file = parserLogDir.resolve("${startedAt.toFileStamp()}.log")
        file.parentFile.mkdirs()
        file.writeText(render(finishedAt))
    }
}

/**
 * Writes [html] to `html/<outcome>/<address>.html` in [parserLogDir], and the trimmed html of [doc]
 * beside it as `<address>-trim.html`, replacing any earlier copies.
 */
fun saveHtml(outcome: PageOutcome, url: Url, html: String, doc: Document? = null) {
    val dir = parserLogDir.resolve("html/${outcome.folder}")
    dir.mkdirs()
    val name = url.toFileName()
    dir.resolve("$name.html").writeText(html)
    doc?.let { dir.resolve("$name-trim.html").writeText(trimmer.trimHtml(it)) }
}

val parserLogDir = File("../logs/parser")

private val trimmer = HtmlTrimmer()

private fun List<PageOutcome>.tally(): String {
    val counts = PageOutcome.entries.mapNotNull { outcome ->
        count { it == outcome }.takeIf { it > 0 }?.let { "${outcome.folder} $it" }
    }
    return "$size" + if (counts.isEmpty()) "" else " (${counts.joinToString(", ")})"
}

private fun Instant.toFileStamp() = toString().substringBefore('.').removeSuffix("Z").replace(':', '-')

private fun Url.toFileName() = "${host.orEmpty()}${toRelativePath()}"
    .replace(Regex("[^A-Za-z0-9._-]+"), "_")
    .trim('_')
    .take(200)
