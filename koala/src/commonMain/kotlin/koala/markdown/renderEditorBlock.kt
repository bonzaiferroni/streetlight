package koala.markdown

import kotlinx.html.P

fun P.renderEditorBlock(block: MarkdownBlock) {
    when (block) {
        is MarkdownBlockImage -> TODO()
        is MarkdownBlockquote -> TODO()
        is MarkdownCodeBlock -> TODO()
        is MarkdownHeading -> TODO()
        MarkdownHorizontalRule -> TODO()
        is MarkdownOrderedList -> TODO()
        is MarkdownUnorderedList -> TODO()
        is MarkdownParagraph -> renderEditorParagraph(block)
        is MarkdownTable -> TODO()
    }
}

fun P.renderEditorParagraph(block: MarkdownParagraph) {
    renderEditorSpans(block.spans)
}

fun P.renderEditorSpans(spans: List<MarkdownSpan>) {

}