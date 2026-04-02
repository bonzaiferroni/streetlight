package koala.dom

import koala.css.ModifierSet
import koala.css.Width100P
import koala.css.addModifiers
import koala.html.Id
import koala.html.Attribute
import koala.html.setId
import koala.html.setAttribute
import koala.model.mapDistinct
import kotlinx.coroutines.flow.Flow
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
    onValue: ((String) -> Unit)? = null,
    flow: Flow<String?>? = null,
    textModifiers: ModifierSet? = null,
    id: Id? = null,
    placeholder: String? = label,
    size: Int = 25,
    onEnter: (() -> Unit)? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLInputElement {
    val parent = div {
        addModifiers(modifiers)
        setAttribute(Attribute.BlockLabel, label?.lowercase())
    }

    var currentValue = ""
    val element = parent.append {
        input {
            addModifiers(Width100P, textModifiers)
            setId(id)
            type = InputType.text
            onValue?.let { callback ->
                onInputFunction = {
                    val element = (it.target as HTMLInputElement)
                    val newValue = element.value
                    if (newValue != currentValue) {
                        if (flow != null) {
                            element.value = currentValue
                        } else {
                            currentValue = newValue
                        }
                        callback(newValue)
                    }
                }
            }
            label?.let {
                attributes["aria-label"] = it
            }
            placeholder?.let {
                this.placeholder = it
            }
            this.size = size.toString()
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

    flow?.let {
        renderScope.launch {
            flow.collect { value ->
                val value = value ?: ""
                if (value != currentValue) {
                    currentValue = value
                    element.value = value
                }
            }
        }
    }

    return element
}

fun <T> WireContext<T>.textField(
    label: String,
    read: (T) -> String?,
    write: (TextUpdate<T>) -> T,
) = textField(
    label = label,
    flow = state.flow.mapDistinct { read(it) ?: "" },
    onValue = { text -> state.set { write(TextUpdate(it, text)) } },
)

data class TextUpdate<T>(
    val state: T,
    val value: String,
)