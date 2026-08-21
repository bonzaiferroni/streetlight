package koala.markdown

internal inline fun String.forEachLine(from: Int, to: Int, action: (from: Int, to: Int) -> Unit) {
    var lineStart = from
    while (lineStart < to) {
        val newline = indexOf('\n', lineStart)
        val lineEnd = when {
            newline == -1 || newline > to -> to
            else -> newline
        }
        action(lineStart, lineEnd)
        lineStart = lineEnd + 1
    }
}

internal inline fun String.forEachLine(action: (from: Int, to: Int) -> Unit) =
    forEachLine(0, length, action)

internal fun String.skip(marker: Char, from: Int, to: Int): Int =
    when {
        from < to && this[from] == marker -> from + 1
        else -> from
    }

internal fun String.contentStart(from: Int, to: Int): Int {
    var start = from
    while (start < to && this[start].isWhitespace()) start++
    return start
}

internal fun String.contentEnd(from: Int, to: Int): Int {
    var end = to
    while (end > from && this[end - 1].isWhitespace()) end--
    return end
}