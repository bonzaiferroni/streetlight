package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.setModifiers
import kotlinx.html.FlowContent
import kotlinx.html.OL
import kotlinx.html.UL
import kotlinx.html.ol
import kotlinx.html.ul

fun FlowContent.olist(
    axis: ListAxis,
    modifiers: ModifierSet? = null,
    block: OL.() -> Unit = {},
) {
    ol {
        configureListing(axis, modifiers)
        block()
    }
}

fun FlowContent.ulist(
    axis: ListAxis,
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
        ListAxis.Row -> ListKey.rowClass
        ListAxis.Column -> ListKey.columnClass
    }
    setModifiers(modifiers, ListKey.baseClass, axisClass)
}

enum class ListAxis {
    Row,
    Column
}

object ListKey {
    val baseClass = Css("listing")
    val rowClass = Css("row-listing")
    val columnClass = Css("column-listing")
}

// language="CSS"
const val LISTING_STYLES = """
.listing {
    display: flex;
    min-width: 0;
    min-height: 0;
    gap: var(--unit-spacing);
}

.column-listing {
    flex-direction: column;
}

.row-listing {
    flex-direction: row;
}
"""