package koala.markdown

import kampfire.api.Markdown
import kampfire.model.toUrl

fun markdownBlocksOf(markdown: Markdown) =
    MarkdownParser().parseBlocks(markdown.value)

class MarkdownParser {

    private val spanParser = MarkdownSpanParser()
    private val listParser = MarkdownListParser(spanParser)
    private val tableParser = MarkdownTableParser(spanParser)
    private val blocks = mutableListOf<ParsedBlock>()
    private val openBlock = OpenBlock()

    fun parseBlocks(text: String, keepBlanks: Boolean = false): List<ParsedBlock> {
        blocks.clear()
        openBlock.reset()

        for (line in text.lineSequence()) {
            val type = openBlock.type
            if (type != null) {
                if (type.closes(line)) {
                    openBlock.add(line)
                    close()
                    continue
                }
                if (type.accepts(line)) {
                    openBlock.add(line)
                    continue
                }
                close()
            }

            if (line.isBlank()) {
                if (keepBlanks) blocks.add(ParsedBlock("", MarkdownParagraph.Empty))
                continue
            }

            markdownBlockTypeOf(line)?.let {
                openBlock.open(it, line)
            }
        }
        close()

        return blocks.toList()
    }

    private fun close() {
        val type = openBlock.type ?: return
        val chunk = openBlock.chunk
        openBlock.reset()

        val markdown = when (type) {
            ContentType.Code -> parseCodeBlock(chunk)
            ContentType.Heading -> parseHeading(chunk)
            ContentType.HorizontalRule -> MarkdownHorizontalRule
            ContentType.Image -> parseBlockImage(chunk)
            ContentType.BlockQuote -> parseBlockquote(chunk)
            ContentType.UnorderedList, ContentType.OrderedList -> listParser.parse(chunk)
            ContentType.Table -> tableParser.parse(chunk)
            ContentType.Paragraph -> null
        } ?: parseParagraph(chunk)

        blocks.add(ParsedBlock(chunk, markdown))
    }

    private fun parseParagraph(chunk: String): MarkdownParagraph {
        val from = chunk.contentStart(0, chunk.length)
        val to = chunk.contentEnd(from, chunk.length)
        if (to <= from) return MarkdownParagraph.Empty
        return MarkdownParagraph(spanParser.parse(chunk, from, to))
    }

    private fun parseHeading(chunk: String): MarkdownHeading? {
        var level = 0
        while (level < chunk.length && chunk[level] == '#') level++
        if (level !in 1..6) return null
        if (level >= chunk.length || chunk[level] != ' ') return null

        val filigree = chunk.endsWith("---")
        val lineTo = when (filigree) {
            true -> chunk.length - 3
            else -> chunk.length
        }
        val from = chunk.contentStart(level, lineTo)
        val to = chunk.contentEnd(from, lineTo)

        return MarkdownHeading(
            level = level,
            filigree = filigree,
            spans = spanParser.parse(chunk, from, to),
        )
    }

    private fun parseBlockquote(chunk: String): MarkdownBlockquote? {
        val paragraphs = mutableListOf<MarkdownParagraph>()

        chunk.forEachLine { lineFrom, lineTo ->
            val marker = chunk.skip('>', lineFrom, lineTo)
            val from = chunk.contentStart(marker, lineTo)
            val to = chunk.contentEnd(from, lineTo)
            if (to > from) {
                paragraphs.add(MarkdownParagraph(spanParser.parse(chunk, from, to)))
            }
        }

        return paragraphs.takeIf { it.isNotEmpty() }
            ?.let { MarkdownBlockquote(paragraphs = it) }
    }

    private fun parseCodeBlock(chunk: String): MarkdownCodeBlock {
        val firstNewline = chunk.indexOf('\n')
        val firstLineEnd = when (firstNewline) {
            -1 -> chunk.length
            else -> firstNewline
        }
        val languageFrom = chunk.contentStart(3, firstLineEnd)
        val languageTo = chunk.contentEnd(languageFrom, firstLineEnd)
        val language = chunk.substring(languageFrom, languageTo).takeIf { it.isNotEmpty() }

        if (firstNewline == -1) {
            return MarkdownCodeBlock(language, "", chunk.length)
        }

        val codeFrom = firstNewline + 1
        val lastLineStart = chunk.lastIndexOf('\n') + 1
        val closed = lastLineStart > firstNewline && chunk.startsWith("```", lastLineStart)
        val codeTo = when (closed) {
            true -> maxOf(codeFrom, lastLineStart - 1)
            else -> chunk.length
        }

        return MarkdownCodeBlock(
            language = language,
            code = chunk.substring(codeFrom, codeTo),
            codeIndex = codeFrom,
        )
    }

    private fun parseBlockImage(chunk: String): MarkdownBlockImage? {
        if (!chunk.startsWith("![")) return null
        val closeBracket = chunk.indexOf(']', 2)
        if (closeBracket == -1) return null
        if (closeBracket + 1 >= chunk.length || chunk[closeBracket + 1] != '(') return null
        val closeParen = chunk.indexOf(')', closeBracket + 2)
        if (closeParen == -1) return null

        val args = parseImageArgs(chunk.substring(closeBracket + 2, closeParen))

        return MarkdownBlockImage(
            altText = chunk.substring(2, closeBracket),
            altTextIndex = 2,
            url = args.url.toUrl(),
            urlIndex = chunk.contentStart(closeBracket + 2, closeParen),
            maxWidthPercent = args.maxWidthPercent,
            type = args.type,
        )
    }
}

private class OpenBlock {
    var type: ContentType? = null
        private set

    val lines = mutableListOf<String>()

    val chunk: String get() = lines.takeIf { it.size == 1 }?.first() ?: lines.joinToString("\n")

    fun open(type: ContentType, firstLine: String) {
        this.type = type
        lines.clear()
        lines.add(firstLine)
    }

    fun add(line: String) {
        lines.add(line)
    }

    fun reset() {
        type = null
    }
}

data class ParsedBlock(
    val chunk: String,
    val markdown: MarkdownBlock
)

fun markdownBlockTypeOf(line: String): ContentType? = when {
    MarkdownRegex.Fence.matches(line) -> ContentType.Code
    MarkdownRegex.HorizontalRule.matches(line) -> ContentType.HorizontalRule
    line.startsWith("#") -> ContentType.Heading
    line.startsWith(">") -> ContentType.BlockQuote
    MarkdownRegex.ImageBlock.matches(line) -> ContentType.Image
    MarkdownRegex.UnorderedItem.matches(line) -> ContentType.UnorderedList
    MarkdownRegex.OrderedItem.matches(line) -> ContentType.OrderedList
    MarkdownRegex.TableLine.matches(line) -> ContentType.Table
    line.isNotBlank() -> ContentType.Paragraph
    else -> null
}

// fun parseParagraph(chunk: String, keepBlanks: Boolean): MarkdownParagraph {
//     val text = if (keepBlanks) chunk else chunk.trim()
//     if (text.isEmpty()) return MarkdownParagraph.Empty
//     return MarkdownParagraph(markdownSpansOf(text))
// }

// fun parseHeading(chunk: String): MarkdownHeading? {
//     val level = chunk.takeWhile { it == '#' }.length
//     if (level !in 1..6) return null
//     if (chunk.length <= level || chunk[level] != ' ') return null
//     val filigree = chunk.endsWith("---")
//     val endIndex = when (filigree) {
//         true -> chunk.length - 3
//         else -> chunk.length
//     }
//     val content = chunk.substring(level + 1, endIndex).trim()
//     return MarkdownHeading(level, filigree, markdownSpansOf(content))
// }

// fun parseBlockquote(lines: List<String>): MarkdownBlockquote? {
//     val paragraphs = lines.map { it.removePrefix(">").removePrefix(" ") }
//         .filter { it.isNotBlank() }
//         .map { MarkdownParagraph(markdownSpansOf(it)) }
//     return paragraphs.takeIf { it.isNotEmpty() }
//         ?.let { MarkdownBlockquote(paragraphs = it) }
// }

// fun parseCodeBlock(lines: List<String>): MarkdownCodeBlock {
//     val language = lines.first().removePrefix("```").trim().takeIf { it.isNotEmpty() }
//     val body = lines.drop(1)
//     val closed = body.lastOrNull()?.let { MarkdownRegex.Fence.matches(it) } == true
//     return MarkdownCodeBlock(
//         language = language,
//         code = when (closed) {
//             true -> body.dropLast(1)
//             else -> body
//         }.joinToString("\n")
//     )
// }

// fun parseBlockImage(chunk: String): MarkdownBlockImage? {
//     val match = MarkdownRegex.ImageBlock.matchEntire(chunk) ?: return null
//     val args = parseImageArgs(match.groupValues[2])
//     return MarkdownBlockImage(
//         altText = match.groupValues[1],
//         url = args.url,
//         maxWidthPercent = args.maxWidthPercent,
//         type = args.type,
//     )
// }
