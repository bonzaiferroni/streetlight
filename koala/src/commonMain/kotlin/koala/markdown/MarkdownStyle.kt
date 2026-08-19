package koala.markdown

import koala.css.Class
import koala.model.MarkdownEditorStyle

object MarkdownStyle {
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

$List {
    list-style: none;
}

$InlineImage {
    max-width: 33%;
    height: auto;
    display: inline-block;
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

${MarkdownEditorStyle.BlockQuote},
blockquote {
    margin-inline-start: 1rem;
    padding-inline-start: 1rem;
    border-inline-start: 2px solid currentColor;
}

""" }