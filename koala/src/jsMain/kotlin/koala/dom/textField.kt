package koala.dom

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.input
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement

fun RenderContext.textField(
    onChangeValue: ((String) -> Unit)? = null,
    binding: Flow<String>? = null,
    block: (INPUT.() -> Unit)? = null
) {
    val element = input {
        type = InputType.text
        onChangeValue?.let { callback ->
            onInputFunction = {
                val v = (it.target as HTMLInputElement).value
                callback(v)
            }
        }
        block?.invoke(this)
    } as HTMLInputElement

    binding?.let {
        renderScope.launch {
            binding.distinctUntilChanged().collect {
                element.value = it
            }
        }
    }
}