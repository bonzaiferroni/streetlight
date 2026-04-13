package koala.markdown

private val UNORDERED_MARKER = Regex("^[-*+]\\s+(.*)$")
private val ORDERED_MARKER = Regex("^(\\d+)\\.\\s+(.*)$")

fun markdownListsOf(lines: List<String>): List<MarkdownList> {
    if (lines.isEmpty()) return emptyList()

    val baseIndent = lines.first().takeWhile { it == ' ' }.length
    val results = mutableListOf<MarkdownList>()
    val currentItems = mutableListOf<MarkdownListItem>()
    val childLines = mutableListOf<String>()
    var currentKind: ListKind? = null
    var currentStart = 1

    fun attachChildrenToLastItem() {
        if (childLines.isEmpty()) return
        val sublist = markdownListsOf(childLines).firstOrNull()
        childLines.clear()
        if (sublist != null && currentItems.isNotEmpty()) {
            val last = currentItems.removeLast()
            currentItems.add(last.copy(sublist = sublist))
        }
    }

    fun flushCurrentList() {
        attachChildrenToLastItem()
        if (currentItems.isEmpty()) return
        val list: MarkdownList = when (currentKind) {
            ListKind.Ordered -> MarkdownOrderedList(startNumber = currentStart, items = currentItems.toList())
            ListKind.Unordered -> MarkdownUnorderedList(items = currentItems.toList())
            null -> return
        }
        results.add(list)
        currentItems.clear()
        currentKind = null
    }

    for (line in lines) {
        val indent = line.takeWhile { it == ' ' }.length

        if (indent > baseIndent) {
            childLines.add(line)
            continue
        }

        attachChildrenToLastItem()

        val trimmed = line.substring(indent)
        val orderedMatch = ORDERED_MARKER.matchEntire(trimmed)
        val unorderedMatch = if (orderedMatch == null) UNORDERED_MARKER.matchEntire(trimmed) else null

        val kind: ListKind
        val content: String
        val number: Int
        when {
            orderedMatch != null -> {
                kind = ListKind.Ordered
                content = orderedMatch.groupValues[2]
                number = orderedMatch.groupValues[1].toInt()
            }
            unorderedMatch != null -> {
                kind = ListKind.Unordered
                content = unorderedMatch.groupValues[1]
                number = 1
            }
            else -> continue
        }

        if (currentKind != null && currentKind != kind) {
            flushCurrentList()
        }

        if (currentKind == null) {
            currentKind = kind
            if (kind == ListKind.Ordered) currentStart = number
        }

        currentItems.add(MarkdownListItem(spans = markdownSpansOf(content)))
    }

    flushCurrentList()
    return results
}