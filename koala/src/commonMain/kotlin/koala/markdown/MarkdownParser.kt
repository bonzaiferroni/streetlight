package koala.markdown

import kampfire.api.Markdown
import kampfire.model.toUrl
import koala.markdown.ContentBlock.BlockQuote
import koala.markdown.ContentBlock.Code
import koala.markdown.ContentBlock.Heading
import koala.markdown.ContentBlock.HorizontalRule
import koala.markdown.ContentBlock.Image
import koala.markdown.ContentBlock.OrderedList
import koala.markdown.ContentBlock.Paragraph
import koala.markdown.ContentBlock.Table
import koala.markdown.ContentBlock.UnorderedList

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
            Code -> parseCodeBlock(chunk)
            Heading -> parseHeading(chunk)
            HorizontalRule -> MarkdownHorizontalRule
            Image -> parseBlockImage(chunk)
            BlockQuote -> parseBlockquote(chunk)
            UnorderedList, OrderedList -> listParser.parse(chunk)
            Table -> tableParser.parse(chunk)
            Paragraph -> null
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
        var citeFrom = -1
        var citeTo = -1

        chunk.forEachLine { lineFrom, lineTo ->
            val marker = chunk.skip('>', lineFrom, lineTo)
            val from = chunk.contentStart(marker, lineTo)
            val to = chunk.contentEnd(from, lineTo)
            if (to > from) {
                if (citeFrom >= 0) {
                    paragraphs.add(MarkdownParagraph(spanParser.parse(chunk, citeFrom, citeTo)))
                    citeFrom = -1
                }
                if (chunk.startsWith("--", from) && to > from + 2) {
                    citeFrom = from
                    citeTo = to
                } else {
                    paragraphs.add(MarkdownParagraph(spanParser.parse(chunk, from, to)))
                }
            }
        }

        val citation = citeFrom.takeIf { it >= 0 }
            ?.let { chunk.substring(chunk.contentStart(it + 2, citeTo), citeTo) }

        return paragraphs.takeIf { it.isNotEmpty() }
            ?.let { MarkdownBlockquote(paragraphs = it, citation = citation) }
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
    var type: ContentBlock? = null
        private set

    val lines = mutableListOf<String>()

    val chunk: String get() = lines.takeIf { it.size == 1 }?.first() ?: lines.joinToString("\n")

    fun open(type: ContentBlock, firstLine: String) {
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

fun ContentBlock.opens(line: String): Boolean = when (this) {
    Image -> MarkdownRegex.ImageBlock.matches(line)
    Heading -> line.startsWith("#")
    HorizontalRule -> MarkdownRegex.HorizontalRule.matches(line)
    Code -> MarkdownRegex.Fence.matches(line)
    BlockQuote -> line.startsWith(">")
    UnorderedList -> MarkdownRegex.UnorderedItem.matches(line)
    OrderedList -> MarkdownRegex.OrderedItem.matches(line)
    Table -> MarkdownRegex.TableLine.matches(line)
    Paragraph -> line.isNotBlank()
}

fun ContentBlock.accepts(line: String): Boolean = when (this) {
    Code -> true
    BlockQuote, UnorderedList, OrderedList, Table -> opens(line)
    Paragraph -> line.isNotBlank() && ContentBlock.entries.none {
        // an image on a line following a paragraph is added as a span
        it != Paragraph && it != Image && it.opens(line)
    }

    Image, Heading, HorizontalRule -> false
}

fun ContentBlock.closes(line: String): Boolean =
    this == Code && line.startsWith("```")

fun markdownBlockTypeOf(line: String): ContentBlock? =
    blockPrecedence.firstOrNull { it.opens(line) }

object MarkdownRegex {
    val HorizontalRule = Regex("""^(-{3,}|\*{3,})\s*$""")
    val UnorderedItem = Regex("""^\s*[-*+_] .*""")
    val TableLine = Regex("""^\s*\|.*\|\s*$""")
    val OrderedItem = Regex("""^\s*\d+\. .*""")
    val ImageBlock = Regex("""^!\[([^\]]*)\]\(([^)]+)\)\s*$""")
    val Fence = Regex("^```.*")
    val UnorderedListMarker = Regex("""^([-*+_])\s+(.*)$""")
    val OrderedListMarker = Regex("""^(\d+)\.\s+(.*)$""")
    val TableDelimiterCell = Regex("""^\s*:?-{3,}:?\s*$""")
}

private val blockPrecedence = listOf(
    Code,
    HorizontalRule,
    Heading,
    BlockQuote,
    Image,
    UnorderedList,
    OrderedList,
    Table,
    Paragraph,
)