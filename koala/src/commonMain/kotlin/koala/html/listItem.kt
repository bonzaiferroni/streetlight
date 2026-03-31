package koala.html

import koala.css.Class
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.LI
import kotlinx.html.OL
import kotlinx.html.UL
import kotlinx.html.li

fun OL.listItem(
    text: String? = null,
    modifiers: ModifierSet? = null,
    block: LI.() -> Unit = {}
) {
    li {
        addModifiers(ListItemKey.Class, modifiers)
        block()
        text?.let {
            +text
        }
    }
}

fun UL.listItem(
    text: String? = null,
    modifiers: ModifierSet? = null,
    block: LI.() -> Unit = {}
) {
    li {
        addModifiers(ListItemKey.Class, modifiers)
        block()
        text?.let {
            +text
        }
    }
}

object ListItemKey {
    val Class = Class("list-item")
}

// language="CSS"
val ListItemCss get() = """
${ListItemKey.Class} {
    min-width: 0;
}
"""