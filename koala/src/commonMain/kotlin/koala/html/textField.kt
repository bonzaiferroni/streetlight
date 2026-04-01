package koala.html

import kotlinx.html.*
import kotlinx.html.id

fun FlowContent.textField(
    id: Id,
    placeholder: String,
    block: (INPUT.() -> Unit)? = null
) {
    textInput {
        this.id = id.identifier
        this.name = id.identifier
        this.placeholder = placeholder

        block?.invoke(this)
    }
}

fun FlowContent.textField(
    id: Id,
    label: String,
    placeholder: String,
    block: (INPUT.() -> Unit)? = null
) {
    textField(id, placeholder) {
        attributes["aria-label"] = label
        setAttribute(Attribute.BlockLabel, label)

        block?.invoke(this)
    }
}