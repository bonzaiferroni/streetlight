package streetlight.compose.markdown

import koala.markdown.MarkdownBlock
import koala.markdown.MarkdownBlockImage
import koala.markdown.MarkdownBlockquote
import koala.markdown.MarkdownCodeBlock
import koala.markdown.MarkdownEmphasis
import koala.markdown.MarkdownHeading
import koala.markdown.MarkdownHorizontalRule
import koala.markdown.MarkdownInlineCode
import koala.markdown.MarkdownInlineImage
import koala.markdown.MarkdownLink
import koala.markdown.MarkdownList
import koala.markdown.MarkdownListItem
import koala.markdown.MarkdownParagraph
import koala.markdown.MarkdownParser
import koala.markdown.MarkdownSpan
import koala.markdown.MarkdownStrikethrough
import koala.markdown.MarkdownStrong
import koala.markdown.MarkdownTable
import koala.markdown.MarkdownText
import koala.markdown.MarkdownUrl

/**
 * The styled ranges of markdown text as an editor shows it, parsed a chunk at a time and kept by each chunk's text, so
 * a change parses again only the chunks it touched. A chunk is a run of lines between blank lines, a fenced code block
 * held whole. The cousin of the web's `MarkdownEditor`, which caches its blocks by their text the same way.
 */
class MarkdownStyler {
    private val parser = MarkdownParser()
    private var cache = mapOf<String, List<StyledRange>>()

    /** The styled ranges of [text], each placed in the whole text. */
    fun stylesOf(text: String): List<StyledRange> {
        val kept = mutableMapOf<String, List<StyledRange>>()
        val ranges = mutableListOf<StyledRange>()
        var offset = 0
        chunksOf(text).forEach { chunk ->
            if (chunk.isNotBlank()) {
                val chunkRanges = kept[chunk] ?: cache[chunk] ?: rangesOf(chunk)
                kept[chunk] = chunkRanges
                chunkRanges.forEach { ranges.add(it.shiftedBy(offset)) }
            }
            offset += chunk.length + 1
        }
        // only the chunks of the text as it stands are kept, so the cache holds no more than the text
        cache = kept
        return ranges
    }

    /** The styled ranges of [chunk], from the blocks it parses into, which its newlines join. */
    private fun rangesOf(chunk: String): List<StyledRange> = buildList {
        var offset = 0
        parser.parseBlocks(chunk, keepBlanks = true).forEach { block ->
            val markdown = block.markdown
            if (markdown is MarkdownHeading) {
                val lineEnd = block.chunk.indexOf('\n').takeIf { it >= 0 } ?: block.chunk.length
                add(StyledRange(offset, offset + lineEnd, MarkdownStyle.Heading(markdown.level)))
            }
            if (markdown is MarkdownCodeBlock) add(StyledRange(offset, offset + block.chunk.length, MarkdownStyle.CodeBlock))
            segmentsOf(block.chunk, markdown.editorSpans()).forEach { add(it.shiftedBy(offset)) }
            offset += block.chunk.length + 1
        }
    }
}

/** A range of markdown text from [from] to [to] and the [style] an editor shows it in. */
data class StyledRange(val from: Int, val to: Int, val style: MarkdownStyle) {
    fun shiftedBy(offset: Int) = copy(from = from + offset, to = to + offset)
}

/** The ways an editor styles a range of markdown text. The cousin of the web's `EditorStyle` classes. */
sealed interface MarkdownStyle {
    /** The syntax around the content, such as `**` or `#`. */
    data object Extra : MarkdownStyle
    data object Text : MarkdownStyle
    data object Strong : MarkdownStyle
    data object Emphasis : MarkdownStyle
    data object InlineCode : MarkdownStyle
    data object Strikethrough : MarkdownStyle
    data object LinkText : MarkdownStyle
    data object Url : MarkdownStyle

    /** A whole fenced code block. */
    data object CodeBlock : MarkdownStyle

    /** The line of a heading of [level] 1 to 6. */
    data class Heading(val level: Int) : MarkdownStyle
}

/**
 * The text of [text] split into chunks at blank lines, each blank line a chunk of its own, a fenced code block held
 * whole. Joined by newlines, the chunks are the text again.
 */
private fun chunksOf(text: String): List<String> {
    val chunks = mutableListOf<String>()
    val lines = mutableListOf<String>()
    var isInFence = false
    fun closeChunk() {
        if (lines.isEmpty()) return
        chunks.add(lines.joinToString("\n"))
        lines.clear()
    }
    text.lineSequence().forEach { line ->
        if (line.trimStart().startsWith(Fence)) isInFence = !isInFence
        when {
            line.isBlank() && !isInFence -> {
                closeChunk()
                chunks.add(line)
            }
            else -> lines.add(line)
        }
    }
    closeChunk()
    return chunks
}

/** The styled ranges of [chunk] from its markdown [spans], the text between them as syntax. The web's `segmentsIn`. */
private fun segmentsOf(chunk: String, spans: Sequence<MarkdownSpan>): List<StyledRange> = buildList {
    var index = 0
    spans.forEach { span ->
        addExtra(index, span.index)
        index = span.index
        val contentEnd = index + span.text.length
        if (contentEnd > index) add(StyledRange(index, contentEnd, span.style))
        index = contentEnd
        if (span is MarkdownUrl) {
            addExtra(index, span.urlIndex)
            val urlEnd = span.urlIndex + span.url.value.length
            add(StyledRange(span.urlIndex, urlEnd, MarkdownStyle.Url))
            index = urlEnd
        }
    }
    addExtra(index, chunk.length)
}

/** Adds the syntax from [from] to [to], leaving its newlines unstyled. */
private fun MutableList<StyledRange>.addExtra(from: Int, to: Int) {
    if (to > from) add(StyledRange(from, to, MarkdownStyle.Extra))
}

/** The spans of this block that the editor styles. The web's `editorSpans`. */
private fun MarkdownBlock.editorSpans(): Sequence<MarkdownSpan> = when (this) {
    is MarkdownParagraph -> spans.asSequence()
    is MarkdownHeading -> spans.asSequence()
    is MarkdownBlockquote -> paragraphs.asSequence().flatMap { it.spans }
    is MarkdownList -> items.asSequence().flatMap { it.editorSpans() }
    is MarkdownTable -> (sequenceOf(header) + rows).flatMap { row -> row.cells.asSequence().flatMap { it.spans } }
    is MarkdownCodeBlock -> when {
        code.isEmpty() -> emptySequence()
        else -> sequenceOf(MarkdownInlineCode(code, codeIndex))
    }
    is MarkdownBlockImage -> sequenceOf(MarkdownInlineImage(altText, altTextIndex, url, urlIndex, width, type))
    MarkdownHorizontalRule -> emptySequence()
}

private fun MarkdownListItem.editorSpans(): Sequence<MarkdownSpan> =
    spans.asSequence() + (sublist?.editorSpans() ?: emptySequence())

private val MarkdownSpan.style: MarkdownStyle
    get() = when (this) {
        is MarkdownStrong -> MarkdownStyle.Strong
        is MarkdownEmphasis -> MarkdownStyle.Emphasis
        is MarkdownInlineCode -> MarkdownStyle.InlineCode
        is MarkdownStrikethrough -> MarkdownStyle.Strikethrough
        is MarkdownLink, is MarkdownInlineImage -> MarkdownStyle.LinkText
        is MarkdownText -> MarkdownStyle.Text
    }

/** The start of a fenced code block's opening and closing lines. */
private const val Fence = "```"
