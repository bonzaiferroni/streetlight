package koala.html

import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.hr

fun FlowContent.spacer(
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        applyModifiers(modifiers)
        block?.invoke(this)
    }
}

fun FlowContent.spacer(
    text: String,
) {
    row(modify(Width100, JustifyCenter)) {
        hr {
            applyModifiers(modify(Width16, Opacity2))
        }
        heading3(text, modify(Opacity4))
        hr {
            applyModifiers(modify(Width16, Opacity2))
        }
    }
}