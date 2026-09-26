package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Element
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.util.data.MutableDataSet
import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.Url
import kampfire.model.toUrl
import streetlight.agent.plainText
import streetlight.agent.queryElement
import streetlight.model.data.OriginId

private val htmlConverter = FlexmarkHtmlConverter.builder(
    MutableDataSet()
        .set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false)
        .set(FlexmarkHtmlConverter.BR_AS_PARA_BREAKS, false)
        .set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false)
        .set(FlexmarkHtmlConverter.OUTPUT_ATTRIBUTES_ID, false)
        .set(FlexmarkHtmlConverter.TYPOGRAPHIC_SMARTS, false)
        .set(FlexmarkHtmlConverter.WRAP_AUTO_LINKS, false)
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
    val kept = paragraphs.fitting("\n\n").ifEmpty { paragraphs.first().split(sentenceBreak).fitting(" ") }
        .ifEmpty { text.take(maxDescriptionChars).substringBeforeLast(' ') + "…" }
    return url?.let { "$kept\n\n[Read more]($it)" } ?: kept
}

/** The leading parts joined by [separator], as many as fit within [maxDescriptionChars]. */
private fun List<String>.fitting(separator: String): String {
    var kept = ""
    for (part in this) {
        val next = if (kept.isEmpty()) part else "$kept$separator$part"
        if (next.length > maxDescriptionChars) break
        kept = next
    }
    return kept
}

private const val maxDescriptionChars = 1000
private val paragraphBreak = Regex("""\n\s*\n""")
private val sentenceBreak = Regex("""(?<=[.!?])\s+""")

/** This title without its bracketed notes, such as "[SOLD OUT]". */
fun String.withoutBracketNotes(): String = replace(bracketNote, " ").replace(whitespaceRun, " ").trim()

private val bracketNote = Regex("""\[[^\]]*]""")
private val whitespaceRun = Regex("""\s+""")

/**
 * The text of the date parts matched in this element by [selectors], each passing [test], joined in order with
 * repeats dropped, or `null` when none match.
 */
fun Element.dateText(vararg selectors: String?, test: (Element) -> Boolean = { true }): String? = selectors
    .mapNotNull { queryElement(it, test).plainText() }
    .distinct()
    .joinToString(" ")
    .ifEmpty { null }

fun <T> List<RawEvent>.isConstant(selector: (RawEvent) -> T?): Boolean {
    if (size < 3) return false
    val values = mapNotNull(selector)
    return values.size == size && values.distinct().size == 1
}

fun OriginId.toRobotsTxtUrl() = "https://${this}/robots.txt".toUrl()