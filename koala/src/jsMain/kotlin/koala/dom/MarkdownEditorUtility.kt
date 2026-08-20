package koala.dom

import koala.external.selection
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.asList

fun placeCaretAcross(targets: List<Pair<HTMLElement, String>>, offset: Int) {
    var remaining = offset
    targets.forEach { (element, chunk) ->
        if (remaining <= chunk.length) {
            element.placeCaret(remaining.coerceAtLeast(0))
            return
        }
        remaining -= chunk.length + 1
    }
    targets.lastOrNull()?.let { (element, chunk) ->
        element.placeCaret(chunk.length)
    }
}

fun HTMLElement.placeCaret(offset: Int) {
    val selection = window.selection() ?: return
    val safeOffset = offset.coerceIn(0, textContent?.length ?: 0)
    val (node, nodeOffset) = textNodeAt(safeOffset) ?: (this to 0)
    val range = document.createRange()
    range.setStart(node, nodeOffset)
    range.collapse(true)
    selection.removeAllRanges()
    selection.addRange(range)
}

private fun Node.textNodeAt(offset: Int): Pair<Node, Int>? {
    var remaining = offset

    fun walk(node: Node): Pair<Node, Int>? {
        if (node.nodeType == Node.TEXT_NODE) {
            val length = node.textContent?.length ?: 0
            if (remaining <= length) return node to remaining
            remaining -= length
            return null
        }
        node.childNodes.asList().forEach { child ->
            walk(child)?.let { return it }
        }
        return null
    }

    return walk(this)
}

private val NORMALIZE = Regex("[\u00A0\u200B\uFEFF\r]|\r\n")

fun HTMLElement.normalizedTextContent(): String {
    val text = textContent ?: return ""
    if (!NORMALIZE.containsMatchIn(text)) return text
    return NORMALIZE.replace(text) {
        when (it.value) {
            "\u00A0" -> " "
            "\r\n", "\r" -> "\n"
            else -> ""
        }
    }
}