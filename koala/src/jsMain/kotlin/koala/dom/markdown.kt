package koala.dom

import koala.css.ModifierSet
import koala.html.configureMarkdown
import koala.markdown.MarkdownBlock
import koala.markdown.markdownBlocksOf
import kotlinx.html.DIV
import kotlinx.html.js.div

fun AppendScope.markdown(
    blocks: List<MarkdownBlock>,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) = div {
    configureMarkdown(blocks, modifiers, block)
}

fun AppendScope.markdown(
    text: String,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) = markdown(markdownBlocksOf(text), modifiers, block)