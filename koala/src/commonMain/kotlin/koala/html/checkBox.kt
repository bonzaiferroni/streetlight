package koala.html

import koala.css.*
import kotlinx.html.*

fun FlowContent.checkBox(
    id: Id,
    label: String,
) {
    row(modify(AlignItemsCenter)) {
        checkBoxInput {
            this.id = id.identifier
            this.name = id.identifier
        }
        textBlock(label)
    }
}