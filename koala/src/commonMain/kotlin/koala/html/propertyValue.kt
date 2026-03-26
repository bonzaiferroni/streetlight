package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.propertyValue(
    propertyName: String,
    propertyValue: String,
    modifiers: ModifierSet? = null,
) {
    p {
        addModifiers(modifiers)

        span(modify(Dim)) {
            +"$propertyName:"
        }
        span(modify(MarginLeft1, OverflowWrapAnywhere)) {
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
        addModifiers(modifiers)
        textBlock("${propertyName}:", modify(OpacityMost, TextAlignRight))
        block()
    }
}