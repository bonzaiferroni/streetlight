package koala.html

import koala.modifier.*
import kotlinx.html.*

fun FlowContent.checkBox(
    id: Id,
    label: String,
) {
    row(AlignItemsCenter) {
        checkBoxInput {
            this.id = id.identifier
            this.name = id.identifier
        }
        textBlock(label)
    }
}