package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.propertyValue(
    propertyName: String,
    propertyValue: String,
) {
    propertyValue(propertyName) {
        paragraph(propertyValue)
    }
}

fun FlowContent.propertyValue(
    propertyName: String,
    block: DIV.() -> Unit
) {
    row {
        paragraph("${propertyName}:", modify(Opacity6, TextAlignRight))
        block()
    }
}