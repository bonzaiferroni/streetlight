package streetlight.agent

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import kotlinx.serialization.Serializable

class HtmlTrimmer {

    /** Trims a copy of [doc], leaving [doc] unchanged. */
    fun trimHtml(doc: Document): TrimResult {
        val trimmed = doc.clone()
        trimmed.outputSettings().prettyPrint(false)
        trimHead(trimmed)
        trimBody(trimmed)
        val runsCollapsed = collapseRuns(trimmed.body())

        val html = trimmed.outerHtml()
        return TrimResult(
            html = html,
            stats = TrimStats(
                rawChars = doc.outerHtml().length,
                trimmedChars = html.length,
                rawTextChars = doc.body().text().length,
                trimmedTextChars = trimmed.body().text().length,
                runsCollapsed = runsCollapsed,
            )
        )
    }

    private fun trimHead(doc: Document) {
        val head = doc.head()
        trimChildren(head, ::keepHeadNode)
    }

    private fun trimBody(doc: Document) {
        val body = doc.body()
        trimChildren(body, ::keepBodyNode)
    }

    private fun trimChildren(
        parent: Element,
        keepNode: (Node) -> Boolean
    ) {
        val children = parent.childNodes().toList()

        for (child in children) {
            trimNode(child, keepNode)
            if (!keepNode(child)) {
                child.remove()
            }
        }
    }

    private fun trimNode(
        node: Node,
        keepNode: (Node) -> Boolean
    ) {
        when (node) {
            is Element -> trimAttributes(node)
            is TextNode -> node.text(node.text())
        }
        if (node is Element) {
            val children = node.childNodes().toList()
            for (child in children) {
                trimNode(child, keepNode)
                if (!keepNode(child)) {
                    child.remove()
                }
            }
        }
    }

    private fun keepHeadNode(node: Node): Boolean {
        return when (node) {
            is TextNode -> !node.text().isBlank()

            is Element -> when (node.tagName().lowercase()) {
                "title" -> hasMeaningfulContent(node)
                "meta" -> keepMeta(node)
                "link" -> keepHeadLink(node)
                else -> false
            }

            else -> false
        }
    }

    private fun keepBodyNode(node: Node): Boolean {
        return when (node) {
            is TextNode -> !node.text().isBlank()

            is Element -> {
                val tag = node.tagName().lowercase()

                if (tag in removableBodyTags) return false

                if (tag == "img") return hasUsefulImage(node)

                hasMeaningfulContent(node)
            }

            else -> false
        }
    }

    private fun keepMeta(element: Element): Boolean {
        if (element.hasAttr("charset") || element.hasAttr("http-equiv")) return false
        val name = element.attr("name").lowercase()
        if (name in removableMetaNames) return false
        return !name.endsWith("-verification") && !name.startsWith("msapplication") && !name.startsWith("apple-")
    }

    private fun trimAttributes(element: Element) {
        element.attributes().toList().forEach { attribute ->
            val key = attribute.key.lowercase()
            val value = attribute.value.trim().replace(whitespace, " ")
            when {
                key in removableAttributes || key.startsWith("on") -> element.removeAttr(attribute.key)
                key.startsWith("data-") && value.length > maxDataValueLength -> element.removeAttr(attribute.key)
                value != attribute.value -> element.attr(attribute.key, value)
            }
        }
    }

    /**
     * Removes each element child past [maxRunLength] in a run of consecutive siblings sharing a tag and first
     * class, returning the number removed.
     */
    private fun collapseRuns(element: Element): Int {
        var signature: String? = null
        var count = 0
        var removed = 0
        element.children().toList().forEach { child ->
            val next = "${child.tagName()}.${child.className().substringBefore(' ')}"
            count = if (next == signature) count + 1 else 1
            signature = next
            if (count > maxRunLength) {
                child.remove()
                removed++
            } else {
                removed += collapseRuns(child)
            }
        }
        return removed
    }

    private fun keepHeadLink(element: Element): Boolean {
        return element.attr("rel").lowercase() == "canonical"
    }

    private fun hasUsefulImage(element: Element): Boolean {
        return element.hasAttr("src") || element.hasAttr("srcset")
    }

    private fun hasMeaningfulContent(element: Element): Boolean {
        if (element.tagName().lowercase() == "img") {
            return hasUsefulImage(element)
        }

        if (element.ownText().isNotBlank()) return true

        return element.childNodes().any { child ->
            when (child) {
                is TextNode -> !child.text().isBlank()
                is Element -> hasMeaningfulContent(child)
                else -> false
            }
        }
    }

    companion object {
        private val removableBodyTags = setOf(
            "script",
            "style",
            "noscript",
            "template",
            "iframe",
            "object",
            "embed",
            "applet",
            "canvas",
            "svg",
            "form",
            "input",
            "button",
            "select",
            "option",
            "textarea",
            "nav",
        )

        private val removableMetaNames = setOf(
            "viewport",
            "theme-color",
            "color-scheme",
            "robots",
            "googlebot",
            "generator",
            "format-detection",
            "referrer",
        )

        private val removableAttributes = setOf(
            "style",
            "srcset",
            "sizes",
            "width",
            "height",
            "loading",
            "decoding",
            "fetchpriority",
            "elementtiming",
            "tabindex",
            "target",
            "rel",
            "crossorigin",
            "referrerpolicy",
            "integrity",
            "nonce",
        )

        private val whitespace = Regex("\\s+")
        private const val maxDataValueLength = 200
        private const val maxRunLength = 10
    }
}

/** The trimmed [html] of a document, with [stats] on what the trim removed. */
data class TrimResult(val html: String, val stats: TrimStats)

/** The size of a document before and after a trim, and the runs of siblings it collapsed. */
@Serializable
data class TrimStats(
    val rawChars: Int,
    val trimmedChars: Int,
    val rawTextChars: Int,
    val trimmedTextChars: Int,
    val runsCollapsed: Int,
)
