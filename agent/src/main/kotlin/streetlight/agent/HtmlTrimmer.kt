package streetlight.agent

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode

class HtmlTrimmer {

    fun trimHtml(html: String): String {
        val doc = Ksoup.parse(html = html)

        trimHead(doc)
        trimBody(doc)

        return doc.outerHtml()
    }

    private fun trimHead(doc: Document) {
        val head = doc.head() ?: return
        trimChildren(head, ::keepHeadNode)
    }

    private fun trimBody(doc: Document) {
        val body = doc.body() ?: return
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

            is Element -> {
                when (node.tagName().lowercase()) {
                    "title" -> hasMeaningfulContent(node)
                    "meta" -> keepMeta(node)
                    "link" -> keepHeadLink(node)
                    else -> false
                }
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
                if (isHidden(node)) return false

                if (tag == "img") return hasUsefulImage(node)

                hasMeaningfulContent(node)
            }

            else -> false
        }
    }

    private fun keepMeta(element: Element): Boolean {
        val name = element.attr("name").lowercase()
        val property = element.attr("property").lowercase()
        val rel = element.attr("rel").lowercase()

        return name in keptMetaNames ||
                property.startsWith("og:") ||
                property.startsWith("twitter:") ||
                rel == "canonical"
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

    private fun isHidden(element: Element): Boolean {
        if (element.hasAttr("hidden")) return true
        if (element.attr("aria-hidden").equals("true", ignoreCase = true)) return true
        return false
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
            "form",
            "input",
            "button",
            "select",
            "option",
            "textarea"
        )

        private val keptMetaNames = setOf(
            "description",
            "author",
            "keywords"
        )
    }
}