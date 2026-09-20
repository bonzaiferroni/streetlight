package koala.html

import koala.modifier.*
import kotlinx.css.pct
import kotlinx.html.*

fun FlowContent.textField(
    label: String? = null,
    mod: ModifierSet? = null,
    textMod: ModifierSet? = null,
    id: Id? = null,
    placeholder: String? = label,
    name: String? = null,
    size: Int = 25,
    maxLength: Int? = null,
    initialValue: String? = null,
    block: (INPUT.() -> Unit)? = null
) {
    box {
        configureTextFieldContainer(label, mod)

        input {
            configureTextFieldInput(id, textMod, placeholder, size, maxLength, name, initialValue)

            block?.invoke(this)
        }
    }
}

fun DIV.configureTextFieldContainer(
    label: String?,
    mod: Modifier?
) {
    addModifiers(mod)
    setAttribute(Attribute.BlockLabel, label?.lowercase())
}

fun INPUT.configureTextFieldInput(
    id: Id?,
    textMod: Modifier?,
    placeholder: String?,
    size: Int,
    maxLength: Int?,
    name: String?,
    initialValue: String?
) {
    setId(id)
    type = InputType.text
    addModifiers(Width(100.pct), textMod)
    placeholder?.let {
        attributes["aria-label"] = it
        this.placeholder = it
    }
    this.size = size.toString()
    maxLength?.let {
        this.maxLength = it.toString()
    }
    name?.let {
        this.name = it
    }
    value = initialValue ?: ""
}