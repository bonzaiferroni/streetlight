package koala.html

import koala.css.ModifierSet
import koala.css.Width100Pct
import koala.css.addModifiers
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
    mod: ModifierSet?
) {
    addModifiers(mod)
    setAttribute(Attribute.BlockLabel, label?.lowercase())
}

fun INPUT.configureTextFieldInput(
    id: Id?,
    textMod: ModifierSet?,
    placeholder: String?,
    size: Int,
    maxLength: Int?,
    name: String?,
    initialValue: String?
) {
    setId(id)
    type = InputType.text
    addModifiers(Width100Pct, textMod)
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