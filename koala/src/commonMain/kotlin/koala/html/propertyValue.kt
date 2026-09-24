package koala.html

import kotlinx.html.*
import koala.modifier.*

/** A line showing [propertyName], dimmed, followed by [propertyValue]. */
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

/** A row showing [propertyName], dimmed, followed by what [block] builds. */
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