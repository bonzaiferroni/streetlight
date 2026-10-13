package koala.layout

import koala.modifier.*
import kotlinx.css.GridTemplateColumns
import kotlinx.css.GridTemplateRows
import kotlinx.css.fr
import kotlinx.html.DIV
import kotlinx.html.FlowContent

/** A grid with the columns of [columns] and the rows of [rows]. */
fun FlowContent.grid(
    columns: GridTemplateColumns = GridTemplateColumns(1.fr, 1.fr),
    mod: Modifier? = null,
    rows: GridTemplateRows? = null,
    content: DIV.() -> Unit,
) {
    div(modify(GridColumns.Class, mod)) {
        configureGridColumns(columns, rows)
        content()
    }
}

internal fun DIV.configureGridColumns(
    columns: GridTemplateColumns,
    rows: GridTemplateRows? = null,
) {
    setStyle(Css.GridTemplateColumns.of(columns))
    rows?.let {
        setStyle(Css.GridTemplateRows.of(it))
    }
}

object GridColumns {
    val Class = Class("grid")

    /** Columns that fill the row, each at least a minimum width. */
    fun repeatFill(minPx: Int) = GridTemplateColumns("repeat(auto-fill, minmax(${minPx}px, 1fr))")
}

//language=CSS
val GridColumnsCss get () = with(GridColumns) { """
$Class {
    display: grid;
    gap: var(--unit);
}
""" }