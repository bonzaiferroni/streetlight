package koala.dom

import koala.css.ModifierSet
import koala.css.Width100
import koala.css.applyModifiers
import koala.html.Id
import koala.html.applyId
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
    modifiers: ModifierSet? = null,
    textModifiers: ModifierSet? = null,
    id: Id? = null,
    placeholder: String? = null,
    binding: Flow<String>? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLInputElement {
    val parent = div {
        applyModifiers(modifiers)
        label?.let {
            blockLabel = it
        }
    }

    val element = parent.append {
        input {
            applyModifiers(Width100, textModifiers)
            applyId(id)
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
            placeholder?.let {
                this.placeholder = it
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