package koala.markdown

fun markdownListOf(lines: List<String>): MarkdownList? {
    val firstLine = lines.firstOrNull() ?: return null
    val baseIndent = firstLine.takeWhile { it == ' ' }.length

    val items = mutableListOf<MarkdownListItem>()
    val childLines = mutableListOf<String>()
    var ordered = false
    var startNumber = 1
    var marker = '-'

    fun attachChildren() {
        if (childLines.isEmpty()) return
        val sublist = markdownListOf(childLines)
        childLines.clear()
        if (sublist != null && items.isNotEmpty()) {
            items.add(items.removeLast().copy(sublist = sublist))
        }
    }

    for (line in lines) {
        val indent = line.takeWhile { it == ' ' }.length
        if (indent > baseIndent) {
            childLines.add(line)
            continue
        }
        attachChildren()

        val trimmed = line.substring(indent)

        val orderedMatch = MarkdownRegex.OrderedListMarker.matchEntire(trimmed)
        if (orderedMatch != null) {
            if (items.isEmpty()) {
                ordered = true
                startNumber = orderedMatch.groupValues[1].toInt()
            }
            items.add(MarkdownListItem(spans = markdownSpansOf(orderedMatch.groupValues[2])))
            continue
        }

        val unorderedMatch = MarkdownRegex.UnorderedListMarker.matchEntire(trimmed) ?: continue
        if (items.isEmpty()) {
            marker = unorderedMatch.groupValues[1][0]
        }
        items.add(MarkdownListItem(spans = markdownSpansOf(unorderedMatch.groupValues[2])))
    }
    attachChildren()

    if (items.isEmpty()) return null

    return when (ordered) {
        true -> MarkdownOrderedList(startNumber = startNumber, items = items.toList())
        else -> MarkdownUnorderedList(marker = marker, items = items.toList())
    }
}