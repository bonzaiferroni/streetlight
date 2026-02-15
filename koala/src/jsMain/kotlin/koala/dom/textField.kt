package koala.dom

import koala.css.ModifierSet
import koala.css.Width100
import koala.css.applyModifiers
import koala.html.Id
import koala.html.applyBlockLabel
import koala.html.applyId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.input
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.events.KeyboardEvent

fun RenderContext.textField(
    label: String? = null,
    modifiers: ModifierSet? = null,
    textModifiers: ModifierSet? = null,
    id: Id? = null,
    onChangeValue: ((String) -> Unit)? = null,
    binding: Flow<String>? = null,
    placeholder: String? = null,
    onEnter: (() -> Unit)? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLInputElement {
    val parent = div {
        applyModifiers(modifiers)
        applyBlockLabel(label)
    }

    var currentValue = ""
    val element = parent.append {
        input {
            applyModifiers(Width100, textModifiers)
            applyId(id)
            type = InputType.text
            onChangeValue?.let { callback ->
                onInputFunction = {
                    val value = (it.target as HTMLInputElement).value
                    if (value != currentValue) {
                        currentValue = value
                        callback(value)
                    }
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

    onEnter?.let {
        element.addEventListener("keydown", { event ->
            val event = event as KeyboardEvent
            if (event.key == "Enter") {
                onEnter()
            }
        })
    }

    binding?.let {
        renderScope.launch {
            binding.collect { value ->
                if (value != currentValue) {
                    currentValue = value
                    element.value = value
                }
            }
        }
    }

    return element
}