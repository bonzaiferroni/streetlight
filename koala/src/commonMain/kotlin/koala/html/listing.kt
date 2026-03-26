package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.OL
import kotlinx.html.UL
import kotlinx.html.ol
import kotlinx.html.ul

fun FlowContent.olist(
    axis: ListAxis = ListAxis.Column,
    modifiers: ModifierSet? = null,
    block: OL.() -> Unit = {},
) {
    ol {
        configureListing(axis, modifiers)
        block()
    }
}

fun FlowContent.ulist(
    axis: ListAxis = ListAxis.Column,
    modifiers: ModifierSet? = null,
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
    val Class = Css("listing")
    val RowClass = Css("row-listing")
    val ColumnClass = Css("column-listing")
}

// language="CSS"
const val LISTING_STYLES = """
.listing {
    display: flex;
    min-width: 0;
    min-height: 0;
    gap: var(--unit-spacing);
    list-style: none;
}

.column-listing {
    flex-direction: column;
}

.row-listing {
    flex-direction: row;
}
"""