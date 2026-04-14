package koala.html

import koala.css.*
import kotlinx.html.FlowContent

fun FlowContent.bulletsOf(modifiers: ModifierSet? = null, vararg text: String) {
    ulist(modify(modifiers, Gap0, ListStyleDisc), ListAxis.Column) {
        text.forEach { text ->
            listItem(text)
        }
    }
}

fun FlowContent.bulletsOf(vararg text: String) {
    bulletsOf(null, *text)
}