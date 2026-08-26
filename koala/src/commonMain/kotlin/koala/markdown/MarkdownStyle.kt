package koala.markdown

import koala.css.Class
import koala.model.EditorStyle

object MarkdownStyle {
    val Container = Class("md")
    val Block = Container.withBemElement("block")
    val Paragraph = Container.withBemElement("paragraph")
    val UnorderedList = Container.withBemElement("unordered-list")
    val OrderedList = Container.withBemElement("ordered-list")
    val InlineImage = Container.withBemElement("inline-image")
    val InlineImageCaption = Container.withBemElement("inline-image-caption")
    val BlockImage = Container.withBemElement("block-image")
    val Blockquote = Container.withBemElement("blockquote")
}

val FloatLeft = Class("float-left")
val FloatRight = Class("float-right")

// language="CSS"
val MarkdownCss get() = with(MarkdownStyle) { """

$FloatRight {
    float: right;
}

$FloatLeft {
    float: left;
}

$Block > * + * {
    margin-top: var(--unit-spacing-2);
}

$OrderedList {
    padding-inline-start: var(--unit-spacing-3);
}

$UnorderedList {
    list-style: none;
}

$InlineImage {
    max-width: 33%;
    height: auto;
    display: inline-block;
}

$Paragraph {
    white-space: pre-line;
}

$BlockImage figcaption,
$InlineImageCaption {
    display: block;
    text-align: center;
    font-style: italic;
    font-size: 1rem;
    color: rgba(var(--ink), .7);
}

$BlockImage {
    margin: 0 auto;
    text-align: center;
}

${EditorStyle.BlockType.selector(ContentBlock.BlockQuote)},
blockquote {
    margin-inline-start: 1rem;
    padding-inline-start: 1rem;
    border-inline-start: 2px solid var(--primary-fg);
}

$Blockquote {
    figcaption {
        text-align: right;
        opacity: var(--opacity-high);
    }
}

""" }