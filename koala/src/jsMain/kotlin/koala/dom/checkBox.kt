package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.input
import kotlinx.html.js.label
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement

fun AppScope.checkBox(
    label: String,
    onChangeValue: ((Boolean) -> Unit)? = null,
    binding: Flow<Boolean>? = null,
    mod: ModifierSet? = null,
    block: (INPUT.() -> Unit)? = null
) = label {
    addModifiers(mod)
    checkBox(onChangeValue, binding, block)
    +label
}

fun AppScope.checkBox(
    onChangeValue: ((Boolean) -> Unit)? = null,
    binding: Flow<Boolean>? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLInputElement {
    val element = input {
        type = InputType.checkBox
        onChangeValue?.let { callback ->
            onInputFunction = {
                val value = (it.target as HTMLInputElement).checked
                callback(value)
            }
        }
        block?.invoke(this)
    } as HTMLInputElement

    binding?.let {
        parentScope.launch {
            binding.distinctUntilChanged().collect {
                element.checked = it
            }
        }
    }

    return element
}