package streetlight.server.daemon.agent

import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Problem

private fun String.normalizeSpace(): String = replace('\u00A0', ' ').trim()

/**
 * The first element matching [selector] that has text, an image, or a meta element's content, and passes [test], or
 * `null`, including when [selector] is invalid.
 */
fun Element.queryElement(selector: String?, test: (Element) -> Boolean = { true }): Element? = selector?.let { query ->
    when (val outcome = tryQuery(query)) {
        is Problem -> null
        is Ok -> outcome.data.firstOrNull { it.hasMeaningfulContent() && test(it) }
    }
}

private fun Element.hasMeaningfulContent(): Boolean =
    text().isNotBlank() || (tagName() == "meta" && attr("content").isNotBlank()) || getAllElements().any { it.isImage() }

private fun Element.isImage() = tagName() == "img" && (hasAttr("src") || hasAttr("srcset"))

/** The text of this element, or its content when it is a meta element. */
fun Element?.plainText(): String? =
    this?.let { if (it.tagName() == "meta") it.attr("content") else it.text() }?.normalizeSpace()?.takeIf { it.isNotEmpty() }

/** The inner html of this element, or its content when it is a meta element. */
fun Element?.innerHtml(): String? =
    this?.let { if (it.tagName() == "meta") it.attr("content") else it.html() }?.takeIf { it.isNotBlank() }

fun Element?.absoluteUrl(attribute: String): String? =
    this?.absUrl(attribute)?.normalizeSpace()?.takeIf { it.isNotEmpty() }

private val boilerplateTags = setOf("nav", "header", "footer", "aside")

private fun Element.isBoilerplate(): Boolean =
    parents().any { it.tagName() in boilerplateTags || it.attr("role") == "navigation" }

private fun Element.linkDensity(): Float {
    if (tagName() == "meta") return 0f
    val total = text().length
    if (total == 0) return 1f
    return select("a").sumOf { it.text().length }.toFloat() / total
}

/** Whether this element reads as prose, and, unless [allowsChrome], sits outside the page's header, footer and nav. */
fun Element?.isPlausibleProse(allowsChrome: Boolean = false): Boolean {
    val element = this ?: return false
    if (!allowsChrome && element.isBoilerplate()) return false
    if (element.linkDensity() > 0.5f) return false
    val text = (if (element.tagName() == "meta") element.attr("content") else element.text()).normalizeSpace()
    return text.contains('.') && text.length > 64 && text.split(" ").size >= 12
}

fun Element?.isPlausibleField(): Boolean {
    val element = this ?: return false
    return !element.isBoilerplate()
}
