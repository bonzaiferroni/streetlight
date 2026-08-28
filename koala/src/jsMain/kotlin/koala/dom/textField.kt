package koala.dom

import koala.css.*
import koala.html.Id
import koala.html.configureTextFieldContainer
import koala.html.configureTextFieldInput
import koala.model.MutableTap
import kotlinx.html.INPUT
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.input
import web.events.addEventListener
import web.html.HTMLElement
import web.html.HTMLInputElement
import web.html.HTMLParagraphElement
import web.keyboard.KEY_DOWN
import web.keyboard.KeyboardEvent

fun ViewScope.textField(
    field: MutableTap<String>,
    label: String? = null,
    mod: ModifierSet? = null,
    textMod: ModifierSet? = null,
    id: Id? = null,
    placeholder: String? = label,
    name: String? = null,
    size: Int = 25,
    maxLength: Int? = null,
    onEnter: (() -> Unit)? = null,
    block: (INPUT.() -> Unit)? = null
): HTMLElement {
    var currentValue = field.now
    lateinit var element: HTMLInputElement
    var maxLengthText: HTMLParagraphElement? = null

    fun display(value: String) {
        currentValue = value
        if (element.value != value) {
            element.value = value
        }
        maxLengthText?.textContent = "${value.length}/$maxLength"
    }

    val parent = box {
        configureTextFieldContainer(label, mod)

        element = input {
            configureTextFieldInput(id, textMod, placeholder, size, maxLength, name, currentValue)

            onInputFunction = {
                val newValue = (it.target as HTMLInputElement).value
                if (newValue != currentValue) {
                    field.set(newValue)
                    display(field.now)
                }
            }
            block?.invoke(this)
        }.asWeb()

        maxLengthText = maxLength?.let {
            textBlock("${currentValue.length}/$it",
                modify(AlignSelfCenter, JustifySelfEnd, TextSmall, OpacityHalf, Padding1, PointerEventsNone, Italic)
            )
        }
    }

    onEnter?.let {
        element.addEventListener(KeyboardEvent.KEY_DOWN, { event ->
            if (event.key == "Enter") {
                onEnter()
            }
        })
    }

    launchEffect(ViewScope::textField) {
        field.flow.collect { value ->
            display(value)
        }
    }

    return parent
}