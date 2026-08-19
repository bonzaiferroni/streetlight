package koala.markdown

fun markdownTableOf(lines: List<String>): MarkdownTable? {
    if (lines.size < 2) return null

    val header = splitRow(lines[0]) ?: return null
    val delimiterCells = splitRow(lines[1]) ?: return null
    if (delimiterCells.size != header.size) return null
    if (delimiterCells.any { !MarkdownRegex.TableDelimiterCell.matches(it) }) return null

    val alignments = delimiterCells.map { cell ->
        val trimmed = cell.trim()
        val left = trimmed.startsWith(":")
        val right = trimmed.endsWith(":")
        when {
            left && right -> MarkdownTableAlignment.Center
            right -> MarkdownTableAlignment.Right
            left -> MarkdownTableAlignment.Left
            else -> MarkdownTableAlignment.None
        }
    }

    val headerRow = MarkdownTableRow(
        header.map { MarkdownTableCell(markdownSpansOf(it.trim())) }
    )

    val bodyRows = lines.drop(2).mapNotNull { line ->
        val cells = splitRow(line) ?: return@mapNotNull null
        val padded = when {
            cells.size < header.size -> cells + List(header.size - cells.size) { "" }
            cells.size > header.size -> cells.take(header.size)
            else -> cells
        }
        MarkdownTableRow(padded.map { MarkdownTableCell(markdownSpansOf(it.trim())) })
    }

    return MarkdownTable(headerRow, alignments, bodyRows)
}

private fun splitRow(line: String): List<String>? {
    val trimmed = line.trim()
    if (!trimmed.startsWith("|") || !trimmed.endsWith("|")) return null
    return trimmed.substring(1, trimmed.length - 1).split("|")
}

enum class MarkdownTableAlignment { Left, Center, Right, None }

data class MarkdownTableCell(val spans: List<MarkdownSpan>)

data class MarkdownTableRow(val cells: List<MarkdownTableCell>)

