package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.setModifiers
import kotlinx.html.LI
import kotlinx.html.OL
import kotlinx.html.UL
import kotlinx.html.li

fun OL.listItem(
    modifiers: ModifierSet? = null,
    block: LI.() -> Unit = {}
) {
    li {
        setModifiers(modifiers)
        block()
    }
}

fun UL.listItem(
    modifiers: ModifierSet? = null,
    block: LI.() -> Unit = {}
) {
    li {
        setModifiers(modifiers)
        block()
    }
}

object ListItemKey {
    val baseClass = Css("list-item")
}

// language="CSS"
const val LIST_ITEM_STYLES = """
.list-item {
    min-width: 0;
}
"""