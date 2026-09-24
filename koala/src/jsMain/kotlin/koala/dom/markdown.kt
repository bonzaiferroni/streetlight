package koala.dom

import kampfire.api.Markdown
import koala.modifier.*
import koala.html.configureMarkdown
import koala.markdown.ParsedBlock
import koala.markdown.markdownBlocksOf
import kotlinx.html.DIV
import kotlinx.html.js.div

/** Markdown already parsed into [blocks], rendered. */
fun AppendScope.markdown(
    blocks: List<ParsedBlock>,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) = div {
    configureMarkdown(blocks, mod, block)
}

/** [text] rendered from markdown. */
fun AppendScope.markdown(
    text: Markdown,
    mod: Modifier? = null,
    block: DIV.() -> Unit = {}
) = markdown(markdownBlocksOf(text), mod, block)