@file:Suppress("RegExpRedundantEscape") // necessary for regex in js context

package koala.markdown

internal enum class ListKind { Ordered, Unordered }

private val HORIZONTAL_RULE = Regex("^(-{3,}|\\*{3,})\\s*$")
private val UNORDERED_ITEM = Regex("^\\s*[-*+_] .*")
private val ORDERED_ITEM = Regex("^\\s*\\d+\\. .*")
private val IMAGE_BLOCK = Regex("^!\\[([^\\]]*)\\]\\(([^)]+)\\)\\s*$")
private val FENCE = Regex("^```.*")

fun markdownBlocksOf(markdown: String): List<MarkdownBlock> {
    val lines = markdown.split("\n")
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraphLines = mutableListOf<String>()
    val quoteLines = mutableListOf<String>()
    val listLines = mutableListOf<String>()
    val fenceLines = mutableListOf<String>()
    var inFence = false
    var fenceLanguage: String? = null

    fun flushParagraph() {
        if (paragraphLines.isNotEmpty()) {
            parseParagraph(paragraphLines.joinToString("\n"))?.let { blocks.add(it) }
            paragraphLines.clear()
        }
    }

    fun flushQuote() {
        if (quoteLines.isNotEmpty()) {
            parseBlockquote(quoteLines)?.let { blocks.add(it) }
            quoteLines.clear()
        }
    }

    fun flushList() {
        if (listLines.isNotEmpty()) {
            blocks.addAll(markdownListsOf(listLines))
            listLines.clear()
        }
    }

    fun flushAll() {
        flushParagraph()
        flushQuote()
        flushList()
    }

    for (line in lines) {
        val trimmed = line.trimStart()

        if (FENCE.matches(trimmed)) {
            flushAll()
            fenceLanguage = trimmed.removePrefix("```").trim().takeIf { it.isNotEmpty() }
            inFence = true
            continue
        }

        if (inFence) {
            if (trimmed.startsWith("```")) {
                blocks.add(parseCodeBlock(fenceLines, fenceLanguage))
                fenceLines.clear()
                fenceLanguage = null
                inFence = false
            } else {
                fenceLines.add(line)
            }
            continue
        }

        if (FENCE.matches(trimmed)) {
            flushAll()
            inFence = true
            continue
        }

        if (line.isBlank()) {
            flushAll()
            continue
        }

        if (HORIZONTAL_RULE.matches(trimmed)) {
            flushAll()
            blocks.add(MarkdownHorizontalRule)
            continue
        }

        if (trimmed.startsWith("#")) {
            flushAll()
            parseHeading(trimmed)?.let { blocks.add(it) }
            continue
        }

        if (IMAGE_BLOCK.matches(trimmed)) {
            flushAll()
            val match = IMAGE_BLOCK.matchEntire(trimmed)!!
            blocks.add(
                MarkdownImage(
                    altText = match.groupValues[1],
                    url = match.groupValues[2]
                )
            )
            continue
        }

        if (trimmed.startsWith("> ") || trimmed == ">") {
            flushParagraph()
            flushList()
            quoteLines.add(trimmed.removePrefix(">").removePrefix(" "))
            continue
        }

        if (UNORDERED_ITEM.matches(line) || ORDERED_ITEM.matches(line)) {
            flushParagraph()
            flushQuote()
            listLines.add(line)
            continue
        }

        flushQuote()
        flushList()
        paragraphLines.add(line)
    }
    flushAll()

    return blocks
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
    if (lines.isEmpty()) return null
    val inner = lines.joinToString("\n")
    val blocks = markdownBlocksOf(inner)
    if (blocks.isEmpty()) return null
    return MarkdownBlockquote(blocks = blocks)
}

fun parseCodeBlock(lines: List<String>, language: String?): MarkdownCodeBlock {
    return MarkdownCodeBlock(
        language = language,
        code = lines.joinToString("\n")
    )
}