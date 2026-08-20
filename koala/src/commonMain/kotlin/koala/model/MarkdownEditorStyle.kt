package koala.model

import koala.css.Class

object MarkdownEditorStyle {
    val Container = Class("markdown-editor")
    val Paragraph = Container.withElement("paragraph")
    val Heading = Container.withElement("heading-line")
    val HorizontalRule = Container.withElement("horizontal-rule")
    val Code = Container.withElement("code-block")
    val BlockQuote = Container.withElement("blockquote")
    val UnorderedList = Container.withElement("unordered-list")
    val OrderedList = Container.withElement("ordered-list")
    val Table = Container.withElement("table")
    val BlockImage = Container.withElement("block-image")

    val Syntax = Container.withElement("syntax")
}

// language="CSS"
val TextEditorCss get() = with(MarkdownEditorStyle) { """
$Container {
    font-family: ui-monospace, Menlo, Consolas, monospace;
    outline: none;
    padding: var(--unit-spacing);
    font-size: var(--paragraph-size);
    background-color: var(--void-bg);
    color: rgb(var(--ink));
    line-height: var(--paragraph-line-height);
    white-space: pre-wrap;

    border: 1px solid var(--void-border);
    border-radius: var(--unit-spacing);
    box-shadow: var(--input-shadow);
    
    > * {
        min-height: 1lh;
    }
}

$Heading {
    font-weight: bold;
    font-size: 2rem;
    text-align: center;
}

$Syntax {
    color: var(--primary-fg);
}
""" }