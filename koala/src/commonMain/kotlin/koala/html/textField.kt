package koala.html

import kotlinx.html.*
import kotlinx.html.id

fun FlowOrInteractiveOrPhrasingContent.textField(
    id: Id,
    placeholder: String,
) {
    textInput {
        this.id = id.value
        this.name = id.value
        this.placeholder = placeholder
    }
}