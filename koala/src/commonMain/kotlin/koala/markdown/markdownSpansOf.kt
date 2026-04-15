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

        // Inline image: ![alt](url)
        if (rest.startsWith("![")) {
            val closeBracket = text.indexOf("]", i + 2)
            if (closeBracket != -1 &&
                closeBracket + 1 < text.length &&
                text[closeBracket + 1] == '('
            ) {
                val closeParen = text.indexOf(")", closeBracket + 2)
                if (closeParen != -1) {
                    flushPlainText()
                    val args = parseImageArgs(text.substring(closeBracket + 2, closeParen))
                    spans.add(
                        MarkdownInlineImage(
                            altText = text.substring(i + 2, closeBracket),
                            url = args.url,
                            maxWidthPercent = args.maxWidthPercent,
                            type = args.type,
                        )
                    )
                    i = closeParen + 1
                    continue
                }
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

data class ParsedImageArgs(
    val url: String,
    val type: ImageType,
    val maxWidthPercent: Int?
)

internal fun parseImageArgs(raw: String): ParsedImageArgs {
    val commaIndex = raw.indexOf(',')
    val url: String
    val width: Int?
    if (commaIndex == -1) {
        url = raw.trim()
        width = null
    } else {
        url = raw.substring(0, commaIndex).trim()
        width = raw.substring(commaIndex + 1).trim().toIntOrNull()
    }
    return ParsedImageArgs(
        url = url,
        type = imageTypeOf(url),
        maxWidthPercent = width
    )
}

private fun imageTypeOf(url: String): ImageType {
    val extension = url.substringAfterLast('.', "").lowercase()
    return ImageType.entries.firstOrNull { extension in it.extensions } ?: ImageType.Image
}

enum class ImageType(vararg extensions: String) {
    Image,
    Lottie("json");
    // Video("mp4", "webm");

    val extensions = extensions.toSet()
}