package koala.markdown

class MarkdownListParser(private val spanParser: MarkdownSpanParser) {

    fun parse(chunk: String, from: Int = 0, to: Int = chunk.length): MarkdownList? {
        val list = OpenList()

        chunk.forEachLine(from, to) { lineFrom, lineTo ->
            val indentEnd = chunk.contentStart(lineFrom, lineTo)
            val indent = indentEnd - lineFrom
            if (list.baseIndent == -1) list.baseIndent = indent

            if (indent > list.baseIndent) {
                list.addChildLine(lineFrom, lineTo)
                return@forEachLine
            }
            list.attachChildren { childFrom, childTo -> parse(chunk, childFrom, childTo) }

            val marker = markerAt(chunk, indentEnd, lineTo) ?: return@forEachLine
            list.add(
                marker = marker,
                spans = spanParser.parse(
                    chunk,
                    marker.contentStart,
                    chunk.contentEnd(marker.contentStart, lineTo),
                ),
            )
        }
        list.attachChildren { childFrom, childTo -> parse(chunk, childFrom, childTo) }

        return list.close()
    }

    private fun markerAt(chunk: String, from: Int, to: Int): ListMarker? {
        if (from >= to) return null
        val marker = chunk[from]

        if (MarkdownUnorderedList.Markers.contains(marker)) {
            if (from + 1 >= to || chunk[from + 1] != ' ') return null
            return ListMarker(
                contentStart = chunk.contentStart(from + 1, to),
                ordered = false,
                number = 1,
                marker = marker,
            )
        }

        if (!marker.isDigit()) return null
        var scan = from
        while (scan < to && chunk[scan].isDigit()) scan++
        if (scan >= to || chunk[scan] != '.') return null
        if (scan + 1 >= to || chunk[scan + 1] != ' ') return null

        return ListMarker(
            contentStart = chunk.contentStart(scan + 1, to),
            ordered = true,
            number = chunk.substring(from, scan).toIntOrNull() ?: 1,
            marker = marker,
        )
    }
}

private class ListMarker(
    val contentStart: Int,
    val ordered: Boolean,
    val number: Int,
    val marker: Char,
)

private class OpenList {
    val items = mutableListOf<MarkdownListItem>()

    var baseIndent = -1

    private var ordered = false
    private var startNumber = 1
    private var marker = '-'
    private var childFrom = -1
    private var childTo = -1

    fun addChildLine(from: Int, to: Int) {
        if (childFrom == -1) childFrom = from
        childTo = to
    }

    fun attachChildren(parse: (Int, Int) -> MarkdownList?) {
        if (childFrom == -1) return
        val sublist = parse(childFrom, childTo)
        childFrom = -1
        if (sublist != null && items.isNotEmpty()) {
            items.add(items.removeLast().copy(sublist = sublist))
        }
    }

    fun add(marker: ListMarker, spans: List<MarkdownSpan>) {
        if (items.isEmpty()) {
            ordered = marker.ordered
            when (marker.ordered) {
                true -> startNumber = marker.number
                else -> this.marker = marker.marker
            }
        }
        items.add(MarkdownListItem(spans = spans))
    }

    fun close(): MarkdownList? {
        if (items.isEmpty()) return null
        return when (ordered) {
            true -> MarkdownOrderedList(startNumber = startNumber, items = items)
            else -> MarkdownUnorderedList(marker = marker, items = items)
        }
    }
}