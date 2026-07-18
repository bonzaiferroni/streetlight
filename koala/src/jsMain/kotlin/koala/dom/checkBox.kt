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

fun ViewScope.checkBox(
    label: String,
    onValue: ((Boolean) -> Unit)? = null,
    flow: Flow<Boolean>? = null,
    mod: ModifierSet? = null,
    block: (INPUT.() -> Unit)? = null
) = label {
    addModifiers(mod)
    checkBox(onValue, flow, block)
    +label
}

fun ViewScope.checkBox(
    onValue: ((Boolean) -> Unit)? = null,
    flow: Flow<Boolean>? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLInputElement {
    val element = input {
        type = InputType.checkBox
        onValue?.let { callback ->
            onInputFunction = {
                val value = (it.target as HTMLInputElement).checked
                callback(value)
            }
        }
        block?.invoke(this)
    } as HTMLInputElement

    flow?.let {
        scope.launch {
            flow.distinctUntilChanged().collect {
                element.checked = it
            }
        }
    }

    return element
}