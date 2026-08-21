package koala.markdown

import kampfire.model.toUrl

class MarkdownSpanParser {

    private val spans = mutableListOf<MarkdownSpan>()

    private var text = ""
    private var to = 0
    private var cursor = 0
    private var plainStart = 0

    fun parse(text: String, from: Int = 0, to: Int = text.length): List<MarkdownSpan> {
        spans.clear()
        this.text = text
        this.to = to
        cursor = from
        plainStart = from

        while (cursor < to) {
            val matched = parseStrong()
                    || parseEmphasis()
                    || parseInlineCode()
                    || parseInlineImage()
                    || parseLink()
            if (!matched) cursor++
        }
        flushText()

        return spans.toList()
    }

    private fun flushText() {
        if (plainStart >= cursor) return
        spans.add(
            MarkdownText(
                text = text.substring(plainStart, cursor),
                index = plainStart,
            )
        )
    }

    private fun emit(span: MarkdownSpan, nextCursor: Int) {
        flushText()
        spans.add(span)
        cursor = nextCursor
        plainStart = nextCursor
    }

    private fun find(needle: String, from: Int): Int {
        val found = text.indexOf(needle, from)
        return when {
            found == -1 || found + needle.length > to -> -1
            else -> found
        }
    }

    private fun parseStrong(): Boolean {
        if (cursor + 1 >= to || !text.startsWith("**", cursor)) return false
        val end = find("**", cursor + 2)
        if (end == -1) return false
        emit(
            MarkdownStrong(
                text = text.substring(cursor + 2, end),
                index = cursor + 2,
            ),
            end + 2,
        )
        return true
    }

    private fun parseEmphasis(): Boolean {
        if (text[cursor] != '*') return false
        val end = find("*", cursor + 1)
        if (end == -1 || end == cursor + 1) return false
        emit(
            MarkdownEmphasis(
                text = text.substring(cursor + 1, end),
                index = cursor + 1,
            ),
            end + 1,
        )
        return true
    }

    private fun parseInlineCode(): Boolean {
        if (text[cursor] != '`') return false
        val end = find("`", cursor + 1)
        if (end == -1) return false
        emit(
            MarkdownInlineCode(
                text = text.substring(cursor + 1, end),
                index = cursor + 1,
            ),
            end + 1,
        )
        return true
    }

    private fun parseInlineImage(): Boolean {
        if (cursor + 1 >= to || !text.startsWith("![", cursor)) return false
        val closeBracket = find("]", cursor + 2)
        if (closeBracket == -1) return false
        if (closeBracket + 1 >= to || text[closeBracket + 1] != '(') return false
        val closeParen = find(")", closeBracket + 2)
        if (closeParen == -1) return false

        var urlStart = closeBracket + 2
        while (urlStart < closeParen && text[urlStart] == ' ') urlStart++

        val args = parseImageArgs(text.substring(closeBracket + 2, closeParen))
        emit(
            MarkdownInlineImage(
                altText = text.substring(cursor + 2, closeBracket),
                altTextIndex = cursor + 2,
                url = args.url.toUrl(),
                urlIndex = urlStart,
                maxWidthPercent = args.maxWidthPercent,
                type = args.type,
            ),
            closeParen + 1,
        )
        return true
    }

    private fun parseLink(): Boolean {
        if (text[cursor] != '[') return false
        val closeBracket = find("]", cursor + 1)
        if (closeBracket == -1) return false
        if (closeBracket + 1 >= to || text[closeBracket + 1] != '(') return false
        val closeParen = find(")", closeBracket + 2)
        if (closeParen == -1) return false

        emit(
            MarkdownLink(
                text = text.substring(cursor + 1, closeBracket),
                index = cursor + 1,
                url = text.substring(closeBracket + 2, closeParen).toUrl(),
                urlIndex = closeBracket + 2,
            ),
            closeParen + 1,
        )
        return true
    }
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