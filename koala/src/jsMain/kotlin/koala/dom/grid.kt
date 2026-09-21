package koala.dom

import koala.modifier.*
import koala.html.GridColumns
import koala.html.configureGridColumns
import kotlinx.css.GridTemplateColumns
import kotlinx.css.fr
import kotlinx.html.DIV

fun AppendScope.grid(
    template: GridTemplateColumns = GridTemplateColumns(1.fr, 1.fr),
    mod: Modifier? = null,
    queryTemplate: GridTemplateColumns = template,
    content: DIV.() -> Unit,
) = div(modify(GridColumns.Class, mod)) {
    configureGridColumns(template, queryTemplate)
    content()
}