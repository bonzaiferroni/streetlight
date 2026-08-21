package koala.dom

import koala.external.selection
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.asList


fun HTMLElement.caretOffset(): Int? {
    val selection = window.selection() ?: return null
    if (selection.rangeCount == 0) return null

    val range = selection.getRangeAt(0)
    if (!contains(range.startContainer)) return null

    val probe = range.cloneRange()
    probe.selectNodeContents(this)
    probe.setEnd(range.startContainer, range.startOffset)
    return probe.toString().length
}

fun HTMLElement.activeChunk(): HTMLElement? {
    val selection = window.selection() ?: return null
    if (selection.rangeCount == 0) return null

    var node: Node? = selection.getRangeAt(0).startContainer
    while (node != null && node.parentNode != this) {
        node = node.parentNode
    }
    return node as? HTMLElement
}fun placeCaretAcross(targets: List<Pair<HTMLElement, String>>, offset: Int) {
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

    val text = node.textContent ?: ""
    val parent = node.parentNode
    if (node !== this && parent != null && nodeOffset == text.length && text.endsWith("\n")) {
        range.setStart(parent, parent.childNodes.asList().indexOf(node) + 1)
    } else {
        range.setStart(node, nodeOffset)
    }

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