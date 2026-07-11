package koala.dom

import kampfire.api.Markdown
import koala.css.ModifierSet
import koala.html.configureMarkdown
import koala.markdown.MarkdownBlock
import koala.markdown.markdownBlocksOf
import kotlinx.html.DIV
import kotlinx.html.js.div

fun TagScope.markdown(
    blocks: List<MarkdownBlock>,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) = div {
    configureMarkdown(blocks, mod, block)
}

fun TagScope.markdown(
    text: Markdown,
    mod: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) = markdown(markdownBlocksOf(text), mod, block)