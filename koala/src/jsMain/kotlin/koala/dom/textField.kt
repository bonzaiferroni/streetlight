package koala.dom

import koala.css.Width100
import koala.css.applyModifiers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.input
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement
import koala.html.blockLabel
import kotlinx.html.dom.append
import kotlinx.html.js.div

fun RenderContext.textField(
    label: String? = null,
    onChangeValue: ((String) -> Unit)? = null,
    binding: Flow<String>? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLInputElement {
    val parent = div {
        label?.let {
            blockLabel = it
        }
    }

    val element = parent.append {
        input {
            applyModifiers(Width100)
            type = InputType.text
            onChangeValue?.let { callback ->
                onInputFunction = {
                    val v = (it.target as HTMLInputElement).value
                    callback(v)
                }
            }
            label?.let {
                attributes["aria-label"] = it
            }
            block?.invoke(this)
        }
    }.first() as HTMLInputElement

    binding?.let {
        renderScope.launch {
            binding.distinctUntilChanged().collect {
                element.value = it
            }
        }
    }

    return element
}