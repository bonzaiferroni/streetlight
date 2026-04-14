package koala.html

import koala.css.*
import koala.markdown.MarkdownBlock
import koala.markdown.markdownBlocksOf
import koala.markdown.renderBlocks
import koala.markdown.renderSpans
import kotlinx.html.FlowContent
import kotlinx.html.DIV
import kotlinx.html.div

fun FlowContent.markdownContent(
    text: String,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    val blocks = markdownBlocksOf(text)
    div {
        configureMarkdownContent(blocks, modifiers, block)
    }
}

fun DIV.configureMarkdownContent(
    blocks: List<MarkdownBlock>,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    addModifiers(modify(Class, Prose), modifiers)
    block()
    renderBlocks(blocks)
}

fun FlowContent.markdownContent(
    blocks: List<MarkdownBlock>,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    div {
        configureMarkdownContent(blocks, modifiers, block)
    }
}

private val Class = Class("markdown-content")

// language="CSS"
val MarkdownContentCss get() = """
$Class {
    display: flex;
    flex-direction: column;
    min-width: 0;
    gap: var(--unit-spacing-2);
}
"""