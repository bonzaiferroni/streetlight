package koala.html

import kotlinx.html.*

fun FlowContent.checkBox(
    id: Id,
    label: String,
) {
    row(AlignItemsCenter) {
        checkBoxInput {
            this.id = id.value
            this.name = id.value
        }
        paragraph(label)
    }
}