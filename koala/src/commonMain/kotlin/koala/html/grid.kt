package koala.html

import koala.css.*
import kotlinx.css.GridTemplateColumns
import kotlinx.css.fr
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.grid(
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

internal fun DIV.configureGridColumns(
    template: GridTemplateColumns,
    queryTemplate: GridTemplateColumns,
) {
    setStyle(Property.GridTemplateColumns.to(template))
    setStyle(GridColumns.QueryTemplate.to(queryTemplate))
}

object GridColumns {
    val Class = Class("grid")

    fun repeatFill(minPx: Int) = GridTemplateColumns("repeat(auto-fill, minmax(${400}px, 1fr))")

    val QueryTemplate = Property<GridTemplateColumns>("grid-query-columns", true)
}

//language=CSS
val GridColumnsCss get () = with(GridColumns) { """
$Class {
    display: grid; 
    gap: var(--unit-spacing);
    
    @media (max-width: 600px) {
        grid-template-columns: var($QueryTemplate) !important;
    }
}
""" }