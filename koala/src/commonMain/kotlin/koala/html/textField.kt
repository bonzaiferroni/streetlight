package koala.html

import koala.css.ElementClass
import koala.css.ElementClass.blockLabel
import koala.css.applyModifiers
import kotlinx.html.*
import kotlinx.html.id

fun FlowOrInteractiveOrPhrasingContent.textField(
    id: Id,
    placeholder: String,
    block: (INPUT.() -> Unit)? = null
) {
    textInput {
        this.id = id.value
        this.name = id.value
        this.placeholder = placeholder

        block?.invoke(this)
    }
}

fun FlowOrInteractiveOrPhrasingContent.textField(
    id: Id,
    label: String,
    placeholder: String,
    block: (INPUT.() -> Unit)? = null
) {
    textField(id, placeholder) {
        attributes["aria-label"] = label
        blockLabel = label

        block?.invoke(this)
    }
}