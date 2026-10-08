package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Element
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.util.data.MutableDataSet
import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.Url
import kampfire.model.toUrl
import kampfire.utils.similarity
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.queryElement
import streetlight.model.data.OriginId

private val htmlConverter = FlexmarkHtmlConverter.builder(
    MutableDataSet()
        .set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false)
        .set(FlexmarkHtmlConverter.BR_AS_PARA_BREAKS, false)
        .set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false)
        .set(FlexmarkHtmlConverter.OUTPUT_ATTRIBUTES_ID, false)
        .set(FlexmarkHtmlConverter.TYPOGRAPHIC_SMARTS, false)
        .set(FlexmarkHtmlConverter.WRAP_AUTO_LINKS, false)
        .set(FlexmarkHtmlConverter.SKIP_CHAR_ESCAPE, true)
).build()

fun htmlToMarkdown(html: String): Markdown? =
    runCatching { htmlConverter.convert(html) }
        .getOrNull()
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.toMarkdown()

/**
 * The whole paragraphs of [text] that fit within [maxDescriptionChars], or its first sentences when its first
 * paragraph does not fit, followed by a link to [url] to read more. [text] is returned whole when it fits.
 */
fun shortenDescription(text: String, url: Url?): String {
    if (text.length <= maxDescriptionChars) return text
    val paragraphs = text.split(paragraphBreak)
    val keptText = paragraphs.fitting("\n\n").ifEmpty { paragraphs.first().split(sentenceBreak).fitting(" ") }
        .ifEmpty { text.take(maxDescriptionChars).substringBeforeLast(' ') + "…" }
    return url?.let { "$keptText\n\n[Read more]($it)" } ?: keptText
}

/** The leading parts joined by [separator], as many as fit within [maxDescriptionChars]. */
private fun List<String>.fitting(separator: String): String {
    var keptText = ""
    for (part in this) {
        val next = if (keptText.isEmpty()) part else "$keptText$separator$part"
        if (next.length > maxDescriptionChars) break
        keptText = next
    }
    return keptText
}

private const val maxDescriptionChars = 1000
private val paragraphBreak = Regex("""\n\s*\n""")
private val sentenceBreak = Regex("""(?<=[.!?])\s+""")

/** This title without its bracketed notes, such as "[SOLD OUT]". */
fun String.withoutBracketNotes(): String = replace(bracketNote, " ").replace(whitespaceRun, " ").trim()

private val bracketNote = Regex("""\[[^\]]*]""")
private val whitespaceRun = Regex("""\s+""")

/**
 * This title without its trailing segments that name one of [places], such as " @ Ball Arena". A segment follows a
 * separator such as " @ " or " | ", and names a place when their words match at [threshold] or more both ways.
 */
fun String.cleanLocationName(vararg places: String?, threshold: Double): String {
    val names = places.filterNotNull()
    var title = this
    while (true) {
        val separator = titleSeparator.findAll(title).lastOrNull() ?: return title
        val segment = title.substring(separator.range.last + 1)
        if (names.none { segment.matchesBothWays(it, threshold) }) return title
        title = title.substring(0, separator.range.first)
    }
}

/**
 * Whether this text and [other] name the same thing, each word of either matching a word of the other well enough that
 * both averages reach [threshold]. Unlike [kampfire.utils.fuzzyMatches], a text held inside a longer one does not match it.
 */
internal fun String.matchesBothWays(other: String, threshold: Double): Boolean =
    minOf(wordCoverage(other), other.wordCoverage(this)) >= threshold

/** The average, over the words of this text, of each word's [similarity] to its closest word in [other]. */
private fun String.wordCoverage(other: String): Double {
    val words = split(nonWordRun).filter { it.isNotEmpty() }
    val otherWords = other.split(nonWordRun).filter { it.isNotEmpty() }
    if (words.isEmpty() || otherWords.isEmpty()) return 0.0
    return words.sumOf { word -> otherWords.maxOf { word.similarity(it) } } / words.size
}

private val titleSeparator = Regex("""\s+[@|\-–—]\s+""")
private val nonWordRun = Regex("""[^\p{L}\p{N}]+""")

/**
 * The text of the date parts matched in this element by [selectors], each passing [test], joined in order with
 * repeats dropped, or `null` when none match.
 */
fun Element.dateText(vararg selectors: String?, test: (Element) -> Boolean = { true }): String? = selectors
    .mapNotNull { queryElement(it, test).plainText() }
    .distinct()
    .joinToString(" ")
    .ifEmpty { null }


fun OriginId.toRobotsTxtUrl() = "https://${this}/robots.txt".toUrl()