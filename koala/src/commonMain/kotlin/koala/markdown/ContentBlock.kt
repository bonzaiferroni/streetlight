@file:Suppress("RegExpRedundantEscape") // necessary for regex in js context

package koala.markdown

import kampfire.model.Labeled
import koala.markdown.ContentBlock.*

enum class ContentBlock {
    Image,
    Paragraph,
    Heading,
    HorizontalRule,
    Code,
    BlockQuote,
    UnorderedList,
    OrderedList,
    Table,
}

enum class HeadingLevel: Labeled {
    H1,
    H2,
    H3,
    H4,
    H5,
    H6;

    override val label get() = name
}

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
        it != Paragraph && it.opens(line)
    }

    Image, Heading, HorizontalRule -> false
}

fun ContentBlock.closes(line: String): Boolean =
    this == Code && line.startsWith("```")

object MarkdownRegex {
    val HorizontalRule = Regex("^(-{3,}|\\*{3,})\\s*$")
    val UnorderedItem = Regex("^\\s*[-*+] .*")
    val TableLine = Regex("^\\s*\\|.*\\|\\s*$")
    val OrderedItem = Regex("^\\s*\\d+\\. .*")
    val ImageBlock = Regex("^!\\[([^\\]]*)\\]\\(([^)]+)\\)\\s*$")
    val Fence = Regex("^```.*")
    val UnorderedListMarker = Regex("^([-*+_])\\s+(.*)$")
    val OrderedListMarker = Regex("^(\\d+)\\.\\s+(.*)$")
    val TableDelimiterCell = Regex("^\\s*:?-{3,}:?\\s*$")
}
