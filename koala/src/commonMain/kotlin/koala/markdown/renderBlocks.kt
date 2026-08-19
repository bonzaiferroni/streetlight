package koala.markdown

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.blockQuote
import kotlinx.html.code
import kotlinx.html.figcaption
import kotlinx.html.figure
import kotlinx.html.hr
import kotlinx.html.ol
import kotlinx.html.pre
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.ul

fun FlowContent.renderBlocks(blocks: List<MarkdownBlock>) {
    div {
        addModifiers(MarkdownClass.Block)
        blocks.forEach { block ->
            when (block) {
                is MarkdownHeading -> renderHeading(block)
                is MarkdownParagraph -> renderParagraph(block)
                is MarkdownBlockquote -> renderBlockquote(block)
                is MarkdownCodeBlock -> renderCodeBlock(block)
                MarkdownHorizontalRule -> renderHorizontalRule()
                is MarkdownBlockImage -> renderImage(block)
                is MarkdownOrderedList -> renderOrderedList(block)
                is MarkdownUnorderedList -> renderUnorderedList(block)
                is MarkdownTable -> renderTable(block)
            }
        }
    }
}

fun FlowContent.renderHeading(heading: MarkdownHeading) {
    val containerMod = modify(MarginTop4)
    val headingMod = modify(TextAlignCenter)
    val block: FlowContent.() -> Unit = {
        when (heading.level) {
            1 -> heading1(modifiers = headingMod) { renderMarkdownSpans(heading.spans) }
            2 -> heading2(modifiers = headingMod) { renderMarkdownSpans(heading.spans) }
            3 -> heading3(modifiers = headingMod) { renderMarkdownSpans(heading.spans) }
            4 -> heading4(modifiers = headingMod) { renderMarkdownSpans(heading.spans) }
            5 -> heading5(modifiers = headingMod) { renderMarkdownSpans(heading.spans) }
            6 -> heading5(modifiers = headingMod) { renderMarkdownSpans(heading.spans) }  // td: support h6
            else -> error("invalid markdown")
        }
    }
    if (heading.filigree) filigree(containerMod, block = block) else div(containerMod, block)
}

fun FlowContent.renderParagraph(block: MarkdownParagraph) {
    textBlock {
        renderMarkdownSpans(block.spans)
    }
}

fun FlowContent.renderBlockquote(block: MarkdownBlockquote) {
    blockQuote {
        renderBlocks(block.blocks)
    }
}

fun FlowContent.renderCodeBlock(block: MarkdownCodeBlock) {
    pre {
        code {
            // td: render language label and syntax highlighting
            +block.code
        }
    }
}

fun FlowContent.renderHorizontalRule() {
    hr { }
}

fun FlowContent.renderImage(block: MarkdownBlockImage) {
    figure {
        addModifiers(MarkdownClass.BlockImage)
        block.maxWidthPercent?.let {
            style = "max-width: $it%;"
        }

        when (block.type) {
            ImageType.Image -> renderBasicImage(block)
            ImageType.Lottie -> renderLottieImage(block)
        }
        figcaption {
            +block.altText
        }
    }
}

fun FlowContent.renderOrderedList(block: MarkdownOrderedList) {
    ol {
        addModifiers(MarkdownClass.List)
        start = block.startNumber.toString()
        
        block.items.forEach { item ->
            listItem {
                span {
                    renderMarkdownSpans(item.spans)
                }
                item.sublist?.let {
                    renderList(it)
                }
            }
        }
    }
}

fun FlowContent.renderList(block: MarkdownList) {
    when (block) {
        is MarkdownOrderedList -> renderOrderedList(block)
        is MarkdownUnorderedList -> renderUnorderedList(block)
    }
}

fun FlowContent.renderUnorderedList(block: MarkdownUnorderedList) {
    val markerMod = when (block.marker) {
        '*' -> ListStyleDisc
        '-' -> ListStyleMinus
        '+' -> ListStylePlus
        else -> null
    }

    val paddingMod = when (block.marker) {
        '_' -> null
        else -> PaddingLeft3
    }

    ul {
        addModifiers(MarkdownClass.List, paddingMod, markerMod)
        block.items.forEach { item ->
            listItem {
                span {
                    renderMarkdownSpans(item.spans)
                }
                item.sublist?.let {
                    renderList(it)
                }
            }
        }
    }
}

fun FlowContent.renderTable(block: MarkdownTable) {
    val hasHeader = block.header.cells.any { it.spans.isNotEmpty() }

    table {
        addModifiers(AlignSelfStart, MoonShadow, BorderRadius2, ZenBg)
        if (hasHeader) {
            thead {
                tr {
                    block.header.cells.forEachIndexed { index, cell ->
                        th {
                            applyAlignment(block.alignments.getOrNull(index))
                            renderMarkdownSpans(cell.spans)
                        }
                    }
                }
            }
        }
        tbody {
            block.rows.forEach { row ->
                tr {
                    row.cells.forEachIndexed { index, cell ->
                        td {
                            applyAlignment(block.alignments.getOrNull(index))
                            renderMarkdownSpans(cell.spans)
                        }
                    }
                }
            }
        }
    }
}

private fun TagContext.applyAlignment(
    alignment: MarkdownTableAlignment?
) {
    when (alignment) {
        MarkdownTableAlignment.Center -> addModifiers(TextAlignCenter)
        MarkdownTableAlignment.Right -> addModifiers(TextAlignRight)
        else -> Unit
    }
}