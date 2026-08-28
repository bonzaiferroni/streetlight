package koala.dom

import kampfire.model.Url
import koala.css.*
import koala.html.Attribute
import koala.html.setAttribute
import koala.markdown.*
import koala.model.EditorStyle
import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.br
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.img
import kotlinx.html.js.div
import kotlinx.html.span
import kotlinx.html.style
import web.html.HTMLElement

fun createEditorBlock(block: ParsedBlock): HTMLElement {
    val div = document.create.div { }.asWeb()
    div.syncAttributes(block)
    div.append {
        renderEditorBlock(block, div)
    }
    return div
}

fun HTMLElement.syncEditorBlock(block: ParsedBlock) {
    syncAttributes(block)
    val segments = block.toEditorSegments()
    if (matchesEditorSegments(block.chunk, segments)) return
    println("rebuilt")
    clear()
    append {
        renderEditorBlock(block, this@syncEditorBlock, segments)
    }
}

private fun AppendScope.renderEditorBlock(block: ParsedBlock, element: HTMLElement) =
    renderEditorBlock(block, element, block.toEditorSegments())

fun HTMLElement.syncAttributes(block: ParsedBlock) {
    setAttribute(EditorStyle.BlockType.to(block.markdown.blockType))
    when (val markdown = block.markdown) {
        is MarkdownHeading -> {
            setAttribute(EditorStyle.HeadingLevel.to(markdown.level))
            when {
                markdown.filigree -> modify(EditorStyle.HeadingFiligree)
                else -> unmodify(EditorStyle.HeadingFiligree)
            }
        }
        is MarkdownParagraph -> {
            syncFirstImage(markdown.spans)
        }
        is MarkdownBlockImage -> {
            syncImage(markdown.url)
        }
        else -> return
    }
}

// every character of the chunk appears exactly once, in order, as a text node
// in SWYG layout, Extra spans will not be displayed. They cannot define layout structure.
// structure lives on wrapper elements, never on segment spans
private fun AppendScope.renderEditorBlock(block: ParsedBlock, element: HTMLElement, segments: List<EditorSegment>) {
    // renderSegmentsBlock(block.chunk, segments ?: error("segments not found"))
    when (val markdown = block.markdown) {
        is MarkdownParagraph -> {
            // renderFirstImage(markdown.spans)
            renderSegmentsBlock(block.chunk, segments)
        }
        else -> renderSegmentsBlock(block.chunk, segments)
    }
}

private fun AppendScope.renderSegmentsBlock(chunk: String, segments: List<EditorSegment>) {
    segments.forEach {
        span(chunk.substring(it.from, it.to), modify(it.mod))
    }
    if (chunk.isEmpty() || chunk.endsWith("\n")) {
        br { }
    }
}

fun HTMLElement.syncFirstImage(spans: List<MarkdownSpan>) =
    syncImage(spans.firstNotNullOfOrNull { it as? MarkdownInlineImage }?.url)

fun HTMLElement.syncImage(url: Url?) {
    if (url == null) {
        unmodify(EditorStyle.WithImage)
        return
    }

    modify(EditorStyle.WithImage)
    setStyle(Property.InlineImage.to(url))
}