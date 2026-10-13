package koala.dom.component

import koala.modifier.*
import kotlinx.css.GridTemplateColumns
import kotlinx.css.GridTemplateRows
import kotlinx.css.fr
import kotlinx.html.DIV
import koala.dom.AppendScope
import koala.component.GridColumns
import koala.component.configureGridColumns

/** A grid with the columns of [template] and the rows of [rowTemplate]. */
fun AppendScope.grid(
    template: GridTemplateColumns = GridTemplateColumns(1.fr, 1.fr),
    mod: Modifier? = null,
    rowTemplate: GridTemplateRows? = null,
    content: DIV.() -> Unit,
) = div(modify(GridColumns.Class, mod)) {
    configureGridColumns(template, rowTemplate)
    content()
}