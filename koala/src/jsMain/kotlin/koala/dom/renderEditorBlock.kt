package koala.dom

import koala.css.Modifier
import koala.css.addModifiers
import koala.css.modify
import koala.markdown.MarkdownBlock
import koala.markdown.ContentType
import koala.markdown.MarkdownHeading
import koala.markdown.ParsedBlock
import koala.model.EditorStyle
import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.br
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.p
import org.w3c.dom.HTMLBRElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

fun createMarkdownElement(block: ParsedBlock): HTMLElement {
    val p = document.create.p { }
    p.syncBlockMod(block.markdown)
    p.append {
        renderEditorBlock(block)
    }
    return p
}

fun HTMLElement.syncMarkdownElement(block: ParsedBlock) {
    syncBlockMod(block.markdown)
    val segments = block.editorSegments()
    if (matchesEditorSegments(block.chunk, segments)) return
    println("rebuilt")
    clear()
    append {
        renderEditorBlock(block.chunk, segments)
    }
}

fun AppendScope.renderEditorBlock(chunk: String, segments: List<EditorSegment>) {
    segments.forEach {
        span(chunk.substring(it.from, it.to), modify(it.mod))
    }
    if (chunk.isEmpty() || chunk.endsWith("\n")) {
        br { }
    }
}

fun AppendScope.renderEditorBlock(block: ParsedBlock) = renderEditorBlock(block.chunk, block.editorSegments())

fun HTMLElement.syncBlockMod(block: MarkdownBlock) {
    val blockMod = block.blockType.contentMod
    setAttributes(block)
    if (!isModified(blockMod)) {
        setModifiers(blockMod)
    }
}

fun HTMLElement.matchesEditorSegments(chunk: String, segments: List<EditorSegment>): Boolean {
    val nodes = childNodes.asList()
    var nodeIndex = 0

    segments.forEach { segment ->
        val node = nodes.getOrNull(nodeIndex++) as? HTMLElement ?: return false
        if (node.tagName != "SPAN") return false
        if (!node.isModified(segment.mod)) return false

        val text = node.textContent ?: return false
        if (text.length != segment.to - segment.from) return false
        if (!chunk.regionMatches(segment.from, text, 0, text.length)) return false
    }

    if (chunk.isEmpty() || chunk.endsWith("\n")) {
        if (nodes.getOrNull(nodeIndex++) !is HTMLBRElement) return false
    }

    return nodeIndex == nodes.size
}

private val ContentType.contentMod: Modifier get() = when (this) {
    ContentType.Image -> EditorStyle.BlockImage
    ContentType.Paragraph -> EditorStyle.Paragraph
    ContentType.Heading -> EditorStyle.Heading
    ContentType.HorizontalRule -> EditorStyle.HorizontalRule
    ContentType.Code -> EditorStyle.Code
    ContentType.BlockQuote -> EditorStyle.BlockQuote
    ContentType.UnorderedList -> EditorStyle.UnorderedList
    ContentType.OrderedList -> EditorStyle.OrderedList
    ContentType.Table -> EditorStyle.Table
}

fun HTMLElement.setAttributes(block: MarkdownBlock) {
    when (block) {
        is MarkdownHeading -> {
            println("${block.level}: ${block.filigree}")
            setAttribute(EditorStyle.HeadingLevel.to(block.level))
            when {
                block.filigree -> modify(EditorStyle.HeadingFiligree)
                else -> unmodify(EditorStyle.HeadingFiligree)
            }
        }
        else -> return
    }
}

