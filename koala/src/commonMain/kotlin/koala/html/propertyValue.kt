package koala.html

import kotlinx.html.*
import koala.modifier.*

fun FlowContent.textProperty(
    propertyName: String,
    propertyValue: String,
    mod: Modifier? = null,
) {
    p {
        addModifiers(mod)

        span(InkDimFg) {
            +"$propertyName:"
        }
        span(modify(MarginLeft(1), OverflowWrapAnywhere)) {
            +propertyValue
        }
    }
}

fun FlowContent.textProperty(
    propertyName: String,
    mod: Modifier? = null,
    block: DIV.() -> Unit
) {
    row(modify(mod, AlignItemsCenter)) {
        textBlock("${propertyName}:", modify(InkDimFg, TextAlignRight))
        block()
    }
}