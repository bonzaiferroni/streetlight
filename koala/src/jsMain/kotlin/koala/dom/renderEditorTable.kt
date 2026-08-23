package koala.dom

import koala.css.Property
import koala.css.modify
import koala.html.span
import koala.markdown.MarkdownSpan
import koala.markdown.MarkdownTable
import koala.markdown.contentEnd
import koala.markdown.forEachLine
import koala.model.EditorStyle
import kotlinx.css.GridTemplateColumns
import org.w3c.dom.HTMLElement

private fun AppendScope.renderEditorTable(chunk: String, element: HTMLElement, block: MarkdownTable) {
    val columns = block.header.cells.size
    element.setStyle(Property.GridTemplateColumns.to(GridTemplateColumns("repeat($columns, max-content) 1fr")))

    val cursor = SpanCursor(block.tableSpans())

    chunk.forEachLine { lineFrom, lineTo ->
        div(modify(EditorStyle.TableRow)) {
            var tailFrom = -1
            var tailTo = -1

            chunk.forEachCellBox(lineFrom, lineTo) { boxFrom, boxTo ->
                when {
                    isRowEndBox(chunk, boxFrom, boxTo) -> {
                        tailFrom = boxFrom
                        tailTo = boxTo
                    }
                    else -> span(modify(EditorStyle.TableCell)) {
                        renderTableBox(chunk, boxFrom, boxTo, cursor.take(boxTo))
                    }
                }
            }

            span(modify(EditorStyle.TableTail)) {
                if (tailFrom >= 0) {
                    renderTableBox(chunk, tailFrom, tailTo, cursor.take(tailTo))
                }
                if (lineTo < chunk.length) {
                    span("\n", modify(EditorStyle.Space))
                }
            }
        }
    }
}

private fun AppendScope.renderTableBox(chunk: String, from: Int, to: Int, spans: List<MarkdownSpan>) {
    segmentsIn(chunk, from, to, spans.asSequence()).forEach {
        span(chunk.substring(it.from, it.to), modify(it.mod))
    }
}

private class SpanCursor(private val spans: List<MarkdownSpan>) {
    private var index = 0
    private val taken = mutableListOf<MarkdownSpan>()

    fun take(to: Int): List<MarkdownSpan> {
        taken.clear()
        while (index < spans.size && spans[index].index < to) {
            taken.add(spans[index])
            index++
        }
        return taken
    }
}

private fun MarkdownTable.tableSpans(): List<MarkdownSpan> = buildList {
    (listOf(header) + rows).forEach { row ->
        row.cells.forEach { addAll(it.spans) }
    }
}

internal inline fun String.forEachCellBox(from: Int, to: Int, action: (from: Int, to: Int) -> Unit) {
    var boxFrom = from
    while (boxFrom < to) {
        val next = indexOf('|', boxFrom + 1)
        val boxTo = when {
            next == -1 || next >= to -> to
            else -> next
        }
        action(boxFrom, boxTo)
        boxFrom = boxTo
    }
}

internal fun isRowEndBox(chunk: String, from: Int, to: Int): Boolean =
    chunk.contentEnd(from + 1, to) <= from + 1