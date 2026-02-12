package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.propertyValue(
    propertyName: String,
    propertyValue: String,
) {
    propertyValue(propertyName) {
        textBlock(propertyValue)
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