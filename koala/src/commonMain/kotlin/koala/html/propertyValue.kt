package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.propertyValue(
    propertyName: String,
    propertyValue: String,
    modifiers: ModifierSet? = null,
) {
    p {
        applyModifiers(modifiers)

        span(modify(Dim)) {
            +"$propertyName:"
        }
        span(modify(MarginLeft1)) {
            +propertyValue
        }
    }
}

fun FlowContent.propertyValue(
    propertyName: String,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit
) {
    row {
        applyModifiers(modifiers)
        textBlock("${propertyName}:", modify(Opacity6, TextAlignRight))
        block()
    }
}