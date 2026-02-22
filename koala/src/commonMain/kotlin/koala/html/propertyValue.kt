package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.propertyValue(
    propertyName: String,
    propertyValue: String,
) {
    p {
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
    block: DIV.() -> Unit
) {
    row {
        textBlock("${propertyName}:", modify(Opacity6, TextAlignRight))
        block()
    }
}