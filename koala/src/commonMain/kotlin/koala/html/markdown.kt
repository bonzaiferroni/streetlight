package koala.html

import koala.css.*
import koala.markdown.MarkdownBlock
import koala.markdown.markdownBlocksOf
import koala.markdown.renderBlocks
import kotlinx.html.FlowContent
import kotlinx.html.DIV
import kotlinx.html.div

fun FlowContent.markdown(
    text: String,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    val blocks = markdownBlocksOf(text)
    div {
        configureMarkdown(blocks, modifiers, block)
    }
}

fun DIV.configureMarkdown(
    blocks: List<MarkdownBlock>,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    addModifiers(modify(MarkdownClass.Container, Prose), modifiers)
    block()
    renderBlocks(blocks)
}

fun FlowContent.markdown(
    blocks: List<MarkdownBlock>,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    div {
        configureMarkdown(blocks, modifiers, block)
    }
}

object MarkdownClass {
    val Container = Class("markdown-content")
    val Block = Class("markdown-block")
    val List = Class("markdown-list")
    val InlineImage = Class("markdown-inline-image")
    val InlineImageCaption = Class("markdown-inline-image-caption")
    val BlockImage = Class("markdown-block-image")
}

val FloatLeft = Class("float-left")
val FloatRight = Class("float-right")

// language="CSS"
val MarkdownCss get() = """

$FloatRight {
    float: right;
}

$FloatLeft {
    float: left;
}

${MarkdownClass.Block} > * + * {
    margin-top: var(--unit-spacing-2);
}

${MarkdownClass.List} {
    list-style: none;
}

${MarkdownClass.InlineImage} {
    max-width: 25%;
    height: auto;
    display: inline-block;
}

${MarkdownClass.BlockImage} figcaption,
${MarkdownClass.InlineImageCaption} {
    display: block;
    text-align: center;
    font-style: italic;
    font-size: 1rem;
    color: rgba(var(--ink), .7);
}

${MarkdownClass.BlockImage} {
    margin: 0;
}

"""