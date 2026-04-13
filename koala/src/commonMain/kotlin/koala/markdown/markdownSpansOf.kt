package koala.markdown

fun markdownSpansOf(text: String): List<MarkdownSpan> {
    val spans = mutableListOf<MarkdownSpan>()
    val plainText = StringBuilder()
    var i = 0

    fun flushPlainText() {
        if (plainText.isNotEmpty()) {
            spans.add(MarkdownText(plainText.toString()))
            plainText.clear()
        }
    }

    while (i < text.length) {
        val rest = text.substring(i)

        // Strong: **text** (must come before emphasis)
        if (rest.startsWith("**")) {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                flushPlainText()
                spans.add(MarkdownStrong(text.substring(i + 2, end)))
                i = end + 2
                continue
            }
        }

        // Emphasis: *text*
        if (rest.startsWith("*")) {
            val end = text.indexOf("*", i + 1)
            if (end != -1) {
                flushPlainText()
                spans.add(MarkdownEmphasis(text.substring(i + 1, end)))
                i = end + 1
                continue
            }
        }

        // Code: `text`
        if (rest.startsWith("`")) {
            val end = text.indexOf("`", i + 1)
            if (end != -1) {
                flushPlainText()
                spans.add(MarkdownInlineCode(text.substring(i + 1, end)))
                i = end + 1
                continue
            }
        }

        /// Link: [text](url)
        if (rest.startsWith("[")) {
            val closeBracket = text.indexOf("]", i + 1)
            if (closeBracket != -1 &&
                closeBracket + 1 < text.length &&
                text[closeBracket + 1] == '('
            ) {
                val closeParen = text.indexOf(")", closeBracket + 2)
                if (closeParen != -1) {
                    flushPlainText()
                    spans.add(
                        parseLink(
                            content = text.substring(i + 1, closeBracket),
                            url = text.substring(closeBracket + 2, closeParen)
                        )
                    )
                    i = closeParen + 1
                    continue
                }
            }
        }

        plainText.append(text[i])
        i++
    }
    flushPlainText();

    return spans
}

fun parseLink(content: String, url: String): MarkdownLink {
    return MarkdownLink(
        spans = markdownSpansOf(content),
        url = url
    )
}