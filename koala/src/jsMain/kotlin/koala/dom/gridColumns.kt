package koala.dom

import koala.css.ModifierSet
import koala.css.modify
import koala.html.GridColumns
import koala.html.configureGridColumns
import kotlinx.css.GridTemplateColumns
import kotlinx.css.fr
import kotlinx.html.DIV

fun TagScope.gridColumns(
    template: GridTemplateColumns = GridTemplateColumns(1.fr, 1.fr),
    queryTemplate: GridTemplateColumns = template,
    mod: ModifierSet? = null,
    content: DIV.() -> Unit,
) {
    div(modify(GridColumns.Class, mod)) {
        configureGridColumns(template, queryTemplate)
        content()
    }
}