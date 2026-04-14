package koala.markdown

import kampfire.model.toUrl
import koala.css.ListStyleDisc
import koala.css.modify
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.blockQuote
import kotlinx.html.code
import kotlinx.html.hr
import kotlinx.html.pre

fun FlowContent.renderBlocks(blocks: List<MarkdownBlock>) {
    column {
        blocks.forEach { block ->
            when (block) {
                is MarkdownHeading -> renderHeading(block)
                is MarkdownParagraph -> renderParagraph(block)
                is MarkdownBlockquote -> renderBlockquote(block)
                is MarkdownCodeBlock -> renderCodeBlock(block)
                MarkdownHorizontalRule -> renderHorizontalRule()
                is MarkdownImage -> renderImage(block)
                is MarkdownOrderedList -> renderOrderedList(block)
                is MarkdownUnorderedList -> renderUnorderedList(block)
            }
        }
    }
}

fun FlowContent.renderHeading(block: MarkdownHeading) {

    when (block.level) {
        1 -> heading1 {
            renderSpans(block.spans)
        }

        2 -> heading2 {
            renderSpans(block.spans)
        }

        3 -> filigree {
            heading3 {
                renderSpans(block.spans)
            }
        }

        4 -> filigree {
            heading4 {
                renderSpans(block.spans)
            }
        }

        5 -> filigree {
            heading5 {
                renderSpans(block.spans)
            }
        }

        6 -> filigree {
            // td: support h6
            heading5 {
                renderSpans(block.spans)
            }
        }

        else -> error("invalid markdown")
    }
}

fun FlowContent.renderParagraph(block: MarkdownParagraph) {
    textBlock {
        renderSpans(block.spans)
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

fun FlowContent.renderImage(block: MarkdownImage) {
    image(block.url.toUrl())
}

fun FlowContent.renderOrderedList(block: MarkdownOrderedList) {
    olist {
        start = block.startNumber.toString()
        
        block.items.forEach { item ->
            listItem {
                span {
                    renderSpans(item.spans)
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
    ulist(modify(ListStyleDisc)) {
        block.items.forEach { item ->
            listItem {
                span {
                    renderSpans(item.spans)
                }
                item.sublist?.let {
                    renderList(it)
                }
            }
        }
    }
}