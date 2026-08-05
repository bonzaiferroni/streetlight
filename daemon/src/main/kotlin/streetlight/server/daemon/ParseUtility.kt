package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Element
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.util.data.MutableDataSet
import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.toUrl
import streetlight.agent.tryQuery
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

private fun String.normalizeSpace(): String = replace('\u00A0', ' ').trim()

fun Element.queryElement(selector: String?): Element? = selector?.let { query ->
    when (val outcome = tryQuery(query)) {
        is Problem -> error(outcome.message)
        is Ok -> outcome.data.firstOrNull()
    }
}

fun Element?.plainText(): String? =
    this?.text()?.normalizeSpace()?.takeIf { it.isNotEmpty() }

fun Element?.innerHtml(): String? =
    this?.html()?.takeIf { it.isNotBlank() }

fun Element?.absoluteUrl(attribute: String): String? =
    this?.absUrl(attribute)?.normalizeSpace()?.takeIf { it.isNotEmpty() }

private val boilerplateTags = setOf("nav", "header", "footer", "aside")

private fun Element.isBoilerplate(): Boolean =
    parents().any { it.tagName() in boilerplateTags || it.attr("role") == "navigation" }

private fun Element.linkDensity(): Float {
    val total = text().length
    if (total == 0) return 1f
    return select("a").sumOf { it.text().length }.toFloat() / total
}

fun Element?.isPlausibleProse(): Boolean {
    val element = this ?: return false
    if (element.isBoilerplate()) return false
    if (element.linkDensity() > 0.5f) return false
    val text = element.text().normalizeSpace()
    return text.contains('.') && text.length > 64 && text.split(" ").size >= 12
}

fun Element?.isPlausibleField(): Boolean {
    val element = this ?: return false
    return !element.isBoilerplate()
}

fun <T> List<RawEvent>.isConstant(selector: (RawEvent) -> T?): Boolean {
    if (size < 3) return false
    val values = mapNotNull(selector)
    return values.size == size && values.distinct().size == 1
}

fun OriginId.toRobotsTxtUrl() = "https://${this}/robots.txt".toUrl()