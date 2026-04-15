@file:Suppress("CssInvalidPropertyValue")

package koala.html

import koala.css.Class
import koala.css.ModifierSet
import koala.css.Prose
import koala.css.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.OL
import kotlinx.html.UL
import kotlinx.html.ol
import kotlinx.html.ul

fun FlowContent.olist(
    modifiers: ModifierSet? = null,
    axis: ListAxis = ListAxis.Column,
    block: OL.() -> Unit = {},
) {
    ol {
        configureListing(axis, modifiers)
        block()
    }
}

fun FlowContent.ulist(
    modifiers: ModifierSet? = null,
    axis: ListAxis = ListAxis.Column,
    block: UL.() -> Unit = {},
) {
    ul {
        configureListing(axis, modifiers)
        block()
    }
}

internal fun TagContext.configureListing(
    axis: ListAxis,
    modifiers: ModifierSet? = null,
) {
    val axisClass = when (axis) {
        ListAxis.Row -> ListKey.RowClass
        ListAxis.Column -> ListKey.ColumnClass
    }
    addModifiers(modifiers, ListKey.Class, axisClass)
}

enum class ListAxis {
    Row,
    Column
}

object ListKey {
    val Class = Class("listing")
    val RowClass = Class("row-listing")
    val ColumnClass = Class("column-listing")
}

val ListStyleDisc = Class("list-style-disc")
val ListStylePlus = Class("list-style-plus")
val ListStyleMinus = Class("list-style-minus")

// language="CSS"
val ListingCss get() = """
${ListKey.Class} {
    display: flex;
    min-width: 0;
    min-height: 0;
    list-style: none;
    gap: var(--unit-spacing);
}

$Prose ${ListKey.Class} {
    gap: 0;
}

${ListKey.ColumnClass} {
    flex-direction: column;
}

${ListKey.RowClass} {
    flex-direction: row;
}

@counter-style minus-marker {
    system: cyclic;
    symbols: "-";
    suffix: " ";
}
@counter-style plus-marker {
    system: cyclic;
    symbols: "+";
    suffix: " ";
}

$ListStyleDisc   { list-style: disc; }
$ListStylePlus   { list-style: plus-marker; }
$ListStyleMinus  { list-style: minus-marker; }
"""