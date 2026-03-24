package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.propertyValue(
    propertyName: String,
    propertyValue: String,
    modifiers: ModifierSet? = null,
) {
    p {
        setModifiers(modifiers)

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
        setModifiers(modifiers)
        textBlock("${propertyName}:", modify(Opacity6, TextAlignRight))
        block()
    }
}