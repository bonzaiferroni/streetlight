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
import org.w3c.dom.HTMLLabelElement

fun RenderContext.checkBox(
    label: String,
    onChangeValue: ((Boolean) -> Unit)? = null,
    binding: Flow<Boolean>? = null,
    modifiers: ModifierSet? = null,
    block: (INPUT.() -> Unit)? = null
) = label {
    addModifiers(modifiers)
    checkBox(onChangeValue, binding, block)
    +label
}

fun RenderContext.checkBox(
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
        renderScope.launch {
            binding.distinctUntilChanged().collect {
                element.checked = it
            }
        }
    }

    return element
}