package koala.dom

import koala.css.ModifierSet
import koala.css.Width100
import koala.css.applyModifiers
import koala.html.Id
import koala.html.applyBlockLabel
import koala.html.applyId
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
    onChangeValue: ((String) -> Unit)? = null,
    bindFlow: Flow<String?>? = null,
    textModifiers: ModifierSet? = null,
    id: Id? = null,
    placeholder: String? = label,
    size: Int = 25,
    onEnter: (() -> Unit)? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLInputElement {
    val parent = div {
        applyModifiers(modifiers)
        applyBlockLabel(label?.lowercase())
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

    bindFlow?.let {
        renderScope.launch {
            bindFlow.collect { value ->
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
    bindFlow = state.flow.mapDistinct { read(it) ?: "" },
    onChangeValue = { text -> state.set { write(TextUpdate(it, text)) } },
)

data class TextUpdate<T>(
    val state: T,
    val value: String,
)