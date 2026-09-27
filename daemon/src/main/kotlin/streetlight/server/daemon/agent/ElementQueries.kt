package streetlight.server.daemon.agent

import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Problem

private fun String.normalizeSpace(): String = replace('\u00A0', ' ').trim()

/** The first element matching [selector] that has text or an image and passes [test], or `null`, including when [selector] is invalid. */
fun Element.queryElement(selector: String?, test: (Element) -> Boolean = { true }): Element? = selector?.let { query ->
    when (val outcome = tryQuery(query)) {
        is Problem -> null
        is Ok -> outcome.data.firstOrNull { it.hasMeaningfulContent() && test(it) }
    }
}

private fun Element.hasMeaningfulContent(): Boolean =
    text().isNotBlank() || getAllElements().any { it.isImage() }

private fun Element.isImage() = tagName() == "img" && (hasAttr("src") || hasAttr("srcset"))

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
