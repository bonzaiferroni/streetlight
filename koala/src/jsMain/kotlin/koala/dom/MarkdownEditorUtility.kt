package koala.dom

import koala.css.Modifier
import koala.css.addModifiers
import koala.dom.renderEditorBlock
import koala.external.selection
import koala.markdown.MarkdownBlockType
import koala.markdown.ParsedBlock
import koala.markdown.accepts
import koala.markdown.markdownBlockTypeOf
import koala.model.MarkdownEditorStyle
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.p
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

fun HTMLElement.syncChunkMod(blockType: MarkdownBlockType) {
    val chunkMod = chunkModOf(blockType)
    if (!isModified(chunkMod)) {
        // console.log("setting mod")
        setModifiers(chunkMod)
    }
}

fun createMarkdownElement(block: ParsedBlock): HTMLElement {
    val (chunk, markdown) = block
    val chunkMod = chunkModOf(markdown.blockType)
    val p = document.create.p {
        addModifiers(chunkMod)
    }
    p.append {
        renderEditorBlock(block)
    }
    return p
}

fun HTMLElement.syncMarkdownElement(block: ParsedBlock) {
    syncChunkMod(block.markdown.blockType)
    clear()
    append {
        renderEditorBlock(block)
    }
}

private fun chunkModOf(blockType: MarkdownBlockType): Modifier = when (blockType) {
    MarkdownBlockType.Image -> MarkdownEditorStyle.BlockImage
    MarkdownBlockType.Paragraph -> MarkdownEditorStyle.Paragraph
    MarkdownBlockType.Heading -> MarkdownEditorStyle.Heading
    MarkdownBlockType.HorizontalRule -> MarkdownEditorStyle.HorizontalRule
    MarkdownBlockType.Code -> MarkdownEditorStyle.Code
    MarkdownBlockType.BlockQuote -> MarkdownEditorStyle.BlockQuote
    MarkdownBlockType.UnorderedList -> MarkdownEditorStyle.UnorderedList
    MarkdownBlockType.OrderedList -> MarkdownEditorStyle.OrderedList
    MarkdownBlockType.Table -> MarkdownEditorStyle.Table
}

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
}