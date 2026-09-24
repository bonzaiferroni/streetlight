@file:Suppress("CssInvalidPropertyValue")

package koala.html

import koala.modifier.Class
import koala.modifier.Modifier
import koala.modifier.Prose
import koala.modifier.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.OL
import kotlinx.html.UL
import kotlinx.html.ol
import kotlinx.html.ul

/** An ordered list laid out along [axis]. */
fun FlowContent.olist(
    mod: Modifier? = null,
    axis: ListAxis = ListAxis.Column,
    block: OL.() -> Unit = {},
) {
    ol {
        configureListing(axis, mod)
        block()
    }
}

/** An unordered list laid out along [axis]. */
fun FlowContent.ulist(
    mod: Modifier? = null,
    axis: ListAxis = ListAxis.Column,
    block: UL.() -> Unit = {},
) {
    ul {
        configureListing(axis, mod)
        block()
    }
}

internal fun TagConfig.configureListing(
    axis: ListAxis,
    mod: Modifier? = null,
) {
    val axisClass = when (axis) {
        ListAxis.Row -> ListKey.RowClass
        ListAxis.Column -> ListKey.ColumnClass
    }
    addModifiers(mod, ListKey.Class, axisClass)
}

/** The direction a list lays out its items. */
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
    gap: var(--unit);
}

ul {
    list-style: none;
}

ol {
    padding-left: var(--unit-3);
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