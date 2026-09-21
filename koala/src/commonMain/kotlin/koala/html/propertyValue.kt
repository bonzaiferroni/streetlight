package koala.html

import kotlinx.html.*
import koala.modifier.*

fun FlowContent.textProperty(
    propertyName: String,
    propertyValue: String,
    modifiers: ModifierSet? = null,
) {
    p {
        addModifiers(modifiers)

        span(modify(InkDimFg)) {
            +"$propertyName:"
        }
        span(modify(MarginLeft(1), OverflowWrapAnywhere)) {
            +propertyValue
        }
    }
}

fun FlowContent.textProperty(
    propertyName: String,
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit
) {
    row(modify(modifiers, AlignItemsCenter)) {
        textBlock("${propertyName}:", modify(InkDimFg, TextAlignRight))
        block()
    }
}