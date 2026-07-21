package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.model.MutableField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.input
import kotlinx.html.js.label
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement

fun ViewScope.checkBox(
    field: MutableField<Boolean>,
    label: String,
    mod: ModifierSet? = null,
    block: (INPUT.() -> Unit)? = null
) = label {
    addModifiers(mod)
    checkBox(field, block)
    +label
}

fun ViewScope.checkBox(
    field: MutableField<Boolean>,
    block: (INPUT.() -> Unit)? = null
): HTMLInputElement {
    var currentValue = field.now
    lateinit var element: HTMLInputElement

    fun display(value: Boolean) {
        currentValue = value
        if (element.checked != value) {
            element.checked = value
        }
    }

    element = input {
        type = InputType.checkBox
        onInputFunction = {
            val newValue = (it.target as HTMLInputElement).checked
            if (newValue != currentValue) {
                field.set(newValue)
                display(field.now)
            }
        }
        checked = currentValue
        block?.invoke(this)
    } as HTMLInputElement

    launchEffect("checkBox") {
        field.flow.collect {
            display(it)
        }
    }

    return element
}