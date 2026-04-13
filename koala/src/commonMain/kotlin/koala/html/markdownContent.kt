package koala.html

import koala.css.*
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
    div {
        addModifiers(Class, modifiers)
        block()

        val blocks = markdownBlocksOf(text)
        renderBlocks(blocks)
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