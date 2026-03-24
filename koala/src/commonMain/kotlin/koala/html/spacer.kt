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
        setModifiers(modifiers)
        block?.invoke(this)
    }
}

fun FlowContent.centeredHeading(
    text: String,
    modifiers: ModifierSet? = null,
) {
    row(modify(modifiers, Width100, JustifyCenter)) {
        hr {
            setModifiers(modify(Width16, OpacitySome))
        }
        heading3(text, modify(OpacityHalf))
        hr {
            setModifiers(modify(Width16, OpacitySome))
        }
    }
}