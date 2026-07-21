package koala.dom

import koala.css.AlignSelfCenter
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
import koala.model.MutableField
import koala.model.StateFieldProto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement
import kotlinx.html.js.input
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLParamElement
import org.w3c.dom.events.KeyboardEvent

fun ViewScope.textField(
    field: MutableField<String>,
    label: String? = null,
    mod: ModifierSet? = null,
    textMod: ModifierSet? = null,
    id: Id? = null,
    placeholder: String? = label,
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
        addModifiers(mod)
        setAttribute(Attribute.BlockLabel, label?.lowercase())

        element = input {
            addModifiers(Width100P, textMod)
            setId(id)
            type = InputType.text
            onInputFunction = {
                val newValue = (it.target as HTMLInputElement).value
                if (newValue != currentValue) {
                    field.set(newValue)
                    display(field.now)
                }
            }
            placeholder?.let {
                attributes["aria-label"] = it
            }
            placeholder?.let {
                this.placeholder = it
            }
            this.size = size.toString()
            value = currentValue
            block?.invoke(this)
        }

        maxLengthText = maxLength?.let {
            textBlock("${currentValue.length}/$it",
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

        launchEffect(ViewScope::textField) {
            field.flow.collect { value ->
                display(value)
            }
        }
    }

    return parent
}