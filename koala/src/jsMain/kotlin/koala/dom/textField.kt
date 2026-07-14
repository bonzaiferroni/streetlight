package koala.dom

import koala.css.AlignSelfCenter
import koala.css.AlignSelfEnd
import koala.css.Italic
import koala.css.JustifySelfEnd
import koala.css.ModifierSet
import koala.css.OpacityHalf
import koala.css.Padding1
import koala.css.PointerEventsNone
import koala.css.TextSmall
import koala.css.Width100P
import koala.css.addModifiers
import koala.css.modify
import koala.html.Id
import koala.html.Attribute
import koala.html.setId
import koala.html.setAttribute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement
import kotlinx.html.js.div
import kotlinx.html.js.input
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.KeyboardEvent

fun AppScope.textField(
    label: String? = null,
    onValue: ((String) -> Unit)? = null,
    flow: Flow<String?>? = null,
    mod: ModifierSet? = null,
    textMod: ModifierSet? = null,
    id: Id? = null,
    placeholder: String? = label,
    size: Int = 25,
    maxLength: Int? = null,
    onEnter: (() -> Unit)? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLElement {
    var currentValue = ""

    val parent = box {
        addModifiers(mod)
        setAttribute(Attribute.BlockLabel, label?.lowercase())

        val element = input {
            addModifiers(Width100P, textMod)
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
        }

        val maxLengthText = maxLength?.let {
            textBlock("0/$it",
                modify(AlignSelfCenter, JustifySelfEnd, TextSmall, OpacityHalf, Padding1, PointerEventsNone, Italic)
            )
        }

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
                        maxLengthText?.textContent = "${value.length}/${maxLength}"
                    }
                }
            }
        }
    }

    return parent
}