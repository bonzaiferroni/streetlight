package koala.component

import koala.modifier.*
import kotlinx.html.*
import koala.layout.row
import koala.page.Id

/** A checkbox with [label] beside it. */
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