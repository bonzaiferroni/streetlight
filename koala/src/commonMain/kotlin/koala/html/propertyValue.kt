package koala.html

import kotlinx.html.*

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
        paragraph("${propertyName}:", Dim, TextAlignRight)
        block()
    }
}