package koala.model

import koala.css.Class

object MarkdownEditorStyle {
    val Class = Class("text-editor")
}

// language="CSS"
val TextEditorCss get() = with(MarkdownEditorStyle) { """
$Class {
    font-family: ui-monospace, Menlo, Consolas, monospace;
    outline: none;
    padding: var(--unit-spacing);
    font-size: var(--paragraph-size);
    background-color: var(--void-bg);
    color: rgb(var(--ink));
    line-height: var(--paragraph-line-height);

    border: 1px solid var(--void-border);
    border-radius: var(--unit-spacing);
    box-shadow: var(--input-shadow);
}
""" }