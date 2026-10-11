package koala.dom.layout

import koala.modifier.*
import kotlinx.css.GridTemplateColumns
import kotlinx.css.GridTemplateRows
import kotlinx.css.fr
import kotlinx.html.DIV
import koala.dom.AppendScope
import koala.layout.GridColumns
import koala.layout.configureGridColumns

/** A grid with the columns of [template], switching to [queryTemplate] on a narrow screen. */
fun AppendScope.grid(
    template: GridTemplateColumns = GridTemplateColumns(1.fr, 1.fr),
    mod: Modifier? = null,
    rowTemplate: GridTemplateRows? = null,
    content: DIV.() -> Unit,
) = div(modify(GridColumns.Class, mod)) {
    configureGridColumns(template, rowTemplate)
    content()
}