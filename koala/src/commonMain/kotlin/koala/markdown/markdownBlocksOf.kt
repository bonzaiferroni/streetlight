package koala.markdown

import kampfire.api.Markdown

fun markdownBlocksOf(markdown: Markdown) = markdownBlocksOf(markdown.value.split("\n"))

fun markdownBlocksOf(lines: List<String>): List<ParsedBlock> {
    val blocks = mutableListOf<ParsedBlock>()
    val open = OpenBlock()

    fun close() {
        open.close()?.let { blocks.add(it) }
    }

    for (line in lines) {
        val type = open.type
        if (type != null) {
            if (type.closes(line)) {
                close()
                continue
            }
            if (type.accepts(line)) {
                open.lines.add(line)
                continue
            }
            close()
        }

        if (line.isBlank()) continue

        markdownBlockTypeOf(line)?.let {
            open.open(it, line)
        }
    }
    close()

    return blocks
}

private class OpenBlock {
    var type: MarkdownBlockType? = null
        private set
    var language: String? = null
        private set

    val lines = mutableListOf<String>()

    fun open(type: MarkdownBlockType, firstLine: String) {
        this.type = type
        lines.clear()
        language = when (type) {
            MarkdownBlockType.Code ->
                firstLine.removePrefix("```").trim().takeIf { it.isNotEmpty() }
            else -> null
        }
        if (type != MarkdownBlockType.Code) lines.add(firstLine)
    }

    fun close(): ParsedBlock? {
        val type = type ?: return null
        this.type = null
        val text = lines.takeIf { it.size == 1 }?.first() ?: lines.joinToString("\n")
        val block = when (type) {
            MarkdownBlockType.Code -> parseCodeBlock(text, language)
            MarkdownBlockType.Heading -> parseHeading(text)
            MarkdownBlockType.HorizontalRule -> MarkdownHorizontalRule
            MarkdownBlockType.Image -> parseBlockImage(text)
            MarkdownBlockType.BlockQuote -> parseBlockquote(lines)
            MarkdownBlockType.UnorderedList, MarkdownBlockType.OrderedList -> markdownListOf(lines)
            MarkdownBlockType.Table -> markdownTableOf(lines)
            MarkdownBlockType.Paragraph -> parseParagraph(text)
        }
        return ParsedBlock(text, block)
    }
}

data class ParsedBlock(
    val chunk: String,
    val markdown: MarkdownBlock?
)

fun markdownBlockTypeOf(line: String): MarkdownBlockType? = when {
    MarkdownRegex.Fence.matches(line) -> MarkdownBlockType.Code
    MarkdownRegex.HorizontalRule.matches(line) -> MarkdownBlockType.HorizontalRule
    line.startsWith("#") -> MarkdownBlockType.Heading
    line.startsWith(">") -> MarkdownBlockType.BlockQuote
    MarkdownRegex.ImageBlock.matches(line) -> MarkdownBlockType.Image
    MarkdownRegex.UnorderedItem.matches(line) -> MarkdownBlockType.UnorderedList
    MarkdownRegex.OrderedItem.matches(line) -> MarkdownBlockType.OrderedList
    MarkdownRegex.TableLine.matches(line) -> MarkdownBlockType.Table
    line.isNotBlank() -> MarkdownBlockType.Paragraph
    else -> null
}

fun parseParagraph(chunk: String): MarkdownParagraph? {
    val text = chunk.trim()
    if (text.isEmpty()) return null
    return MarkdownParagraph(markdownSpansOf(text))
}

fun parseHeading(chunk: String): MarkdownHeading? {
    val level = chunk.takeWhile { it == '#' }.length
    if (level !in 1..6) return null
    if (chunk.length <= level || chunk[level] != ' ') return null
    val filigree = chunk.endsWith("---")
    val endIndex = when (filigree) {
        true -> chunk.length - 3
        else -> chunk.length
    }
    val content = chunk.substring(level + 1, endIndex).trim()
    return MarkdownHeading(level, filigree, markdownSpansOf(content))
}

fun parseBlockquote(lines: List<String>): MarkdownBlockquote? {
    val paragraphs = lines.map { it.removePrefix(">").removePrefix(" ") }
        .filter { it.isNotBlank() }
        .map { MarkdownParagraph(markdownSpansOf(it)) }
    return paragraphs.takeIf { it.isNotEmpty() }
        ?.let { MarkdownBlockquote(paragraphs = it) }
}

fun parseCodeBlock(text: String, language: String?): MarkdownCodeBlock {
    return MarkdownCodeBlock(
        language = language,
        code = text
    )
}

fun parseBlockImage(chunk: String): MarkdownBlockImage? {
    val match = MarkdownRegex.ImageBlock.matchEntire(chunk) ?: return null
    val args = parseImageArgs(match.groupValues[2])
    return MarkdownBlockImage(
        altText = match.groupValues[1],
        url = args.url,
        maxWidthPercent = args.maxWidthPercent,
        type = args.type,
    )
}
