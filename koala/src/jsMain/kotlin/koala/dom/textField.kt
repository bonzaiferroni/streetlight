package koala.dom

import koala.css.ModifierSet
import koala.css.Width100P
import koala.css.addModifiers
import koala.html.Id
import koala.html.Attribute
import koala.html.setId
import koala.html.setAttribute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.input
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.KeyboardEvent

fun AppScope.textField(
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
): HTMLElement {
    val parent = div {
        addModifiers(modifiers)
        setAttribute(Attribute.BlockLabel, label?.lowercase())

        textFieldInput(
            placeholder = placeholder,
            modifiers = textModifiers,
            onValue = onValue,
            flow = flow,
            id = id,
            size = size,
            onEnter = onEnter,
            block = block
        )
    }

    return parent
}

fun AppScope.textFieldInput(
    placeholder: String? = null,
    modifiers: ModifierSet? = null,
    onValue: ((String) -> Unit)? = null,
    flow: Flow<String?>? = null,
    id: Id? = null,
    size: Int = 25,
    onEnter: (() -> Unit)? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLElement {
    var currentValue = ""
    val element = input {
        addModifiers(Width100P, modifiers)
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
        placeholder?.let {
            attributes["aria-label"] = it
        }
        placeholder?.let {
            this.placeholder = it
        }
        this.size = size.toString()
        block?.invoke(this)
    } as HTMLInputElement

    onEnter?.let {
        element.addEventListener("keydown", { event ->
            val event = event as KeyboardEvent
            if (event.key == "Enter") {
                onEnter()
            }
        })
    }

    flow?.let {
        parentScope.launch {
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