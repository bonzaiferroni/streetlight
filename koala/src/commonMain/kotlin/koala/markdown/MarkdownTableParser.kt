package koala.markdown

class MarkdownTableParser(private val spanParser: MarkdownSpanParser) {

    fun parse(chunk: String, from: Int = 0, to: Int = chunk.length): MarkdownTable? {
        var header: MarkdownTableRow? = null
        var alignments: List<MarkdownTableAlignment> = emptyList()
        val rows = mutableListOf<MarkdownTableRow>()
        var lineIndex = 0

        chunk.forEachLine(from, to) { lineFrom, lineTo ->
            when {
                lineIndex == 0 -> header = parseRow(chunk, lineFrom, lineTo)
                lineIndex == 1 && isDelimiterRow(chunk, lineFrom, lineTo) ->
                    alignments = parseAlignments(chunk, lineFrom, lineTo)
                else -> rows.add(parseRow(chunk, lineFrom, lineTo))
            }
            lineIndex++
        }

        return header?.let {
            MarkdownTable(header = it, alignments = alignments, rows = rows)
        }
    }

    private fun parseRow(chunk: String, from: Int, to: Int): MarkdownTableRow {
        val cells = mutableListOf<MarkdownTableCell>()
        chunk.forEachCell(from, to) { cellFrom, cellTo ->
            val contentFrom = chunk.contentStart(cellFrom, cellTo)
            val contentTo = chunk.contentEnd(contentFrom, cellTo)
            cells.add(MarkdownTableCell(spanParser.parse(chunk, contentFrom, contentTo)))
        }
        return MarkdownTableRow(cells = cells)
    }

    private fun parseAlignments(chunk: String, from: Int, to: Int): List<MarkdownTableAlignment> {
        val alignments = mutableListOf<MarkdownTableAlignment>()
        chunk.forEachCell(from, to) { cellFrom, cellTo ->
            val start = chunk.contentStart(cellFrom, cellTo)
            val end = chunk.contentEnd(start, cellTo)
            alignments.add(
                when {
                    start >= end -> MarkdownTableAlignment.None
                    chunk[start] == ':' && chunk[end - 1] == ':' -> MarkdownTableAlignment.Center
                    chunk[start] == ':' -> MarkdownTableAlignment.Left
                    chunk[end - 1] == ':' -> MarkdownTableAlignment.Right
                    else -> MarkdownTableAlignment.None
                }
            )
        }
        return alignments
    }

    private fun isDelimiterRow(chunk: String, from: Int, to: Int): Boolean {
        var found = false
        var valid = true
        chunk.forEachCell(from, to) { cellFrom, cellTo ->
            found = true
            if (!isDelimiterCell(chunk, cellFrom, cellTo)) valid = false
        }
        return found && valid
    }

    private fun isDelimiterCell(chunk: String, from: Int, to: Int): Boolean {
        var scan = chunk.contentStart(from, to)
        val end = chunk.contentEnd(scan, to)
        if (scan >= end) return false

        if (chunk[scan] == ':') scan++
        var dashes = 0
        while (scan < end && chunk[scan] == '-') {
            scan++
            dashes++
        }
        if (dashes < 3) return false
        if (scan < end && chunk[scan] == ':') scan++

        return scan == end
    }
}

internal inline fun String.forEachCell(from: Int, to: Int, action: (from: Int, to: Int) -> Unit) {
    var start = contentStart(from, to)
    var end = contentEnd(start, to)
    if (start < end && this[start] == '|') start++
    if (end > start && this[end - 1] == '|') end--

    var cellFrom = start
    while (true) {
        val pipe = indexOf('|', cellFrom)
        val cellTo = when {
            pipe == -1 || pipe > end -> end
            else -> pipe
        }
        action(cellFrom, cellTo)
        if (cellTo >= end) return
        cellFrom = cellTo + 1
    }
}


