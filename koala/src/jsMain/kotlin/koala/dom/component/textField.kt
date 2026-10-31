package koala.dom.component

import koala.SvgPack
import koala.component.IconStyle
import koala.modifier.*
import koala.page.Id
import kotlinx.css.LinearDimension
import kampfire.model.MutableTap
import kotlinx.html.INPUT
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.input
import web.events.addEventListener
import web.html.HTMLElement
import web.html.HTMLInputElement
import web.html.HTMLParagraphElement
import web.keyboard.KEY_DOWN
import web.keyboard.KeyboardEvent
import koala.dom.ViewScope
import koala.dom.asWeb
import koala.dom.launchEffect
import koala.component.configureTextFieldContainer
import koala.component.configureTextFieldInput

/**
 * A text input bound to [field], labeled [label] over its corner.
 *
 * With [maxLength], a count of characters used is shown. With [onEnterSubmit], Enter submits. [icon] is shown a unit
 * in from the start, naming what the field holds, and [suffix] at the end, outside the input's value.
 */
fun ViewScope.textField(
    field: MutableTap<String>,
    label: String? = null,
    mod: Modifier? = null,
    textMod: Modifier? = null,
    id: Id? = null,
    placeholder: String? = label,
    name: String? = null,
    size: Int = 25,
    maxLength: Int? = null,
    suffix: String? = null,
    onEnterSubmit: (() -> Unit)? = null,
    icon: SvgPack? = null,
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
            val affixMod = modify(
                icon?.let { Css.PaddingLeft(IconInsetUnits) },
                suffix?.let { Css.PaddingRight.of(it.toAffixInset()) },
            )
            configureTextFieldInput(id, modify(affixMod, textMod), placeholder, size, maxLength, name, currentValue)

            onInputFunction = {
                val newValue = (it.target as HTMLInputElement).value
                if (newValue != currentValue) {
                    field.set(newValue)
                    display(field.now)
                }
            }
            block?.invoke(this)
        }.asWeb()

        icon?.let {
            this@textField.icon(it.small, modify(IconStyle.DefaultMod, JustifySelfStart, AlignSelfCenter, MarginLeft(1), OpacityHalf, PointerEventsNone))
        }
        suffix?.let { textBlock(it, modify(TextFieldAffixMod, JustifySelfEnd)) }

        maxLengthText = maxLength?.let {
            textBlock("${currentValue.length}/$it",
                modify(AlignSelfCenter, JustifySelfEnd, TextSmall, OpacityHalf, Padding(1), PointerEventsNone, Italic)
            )
        }
    }

    onEnterSubmit?.let {
        element.addEventListener(KeyboardEvent.KEY_DOWN, { event ->
            event.handleEnterSubmit(onEnterSubmit)
        })
    }

    launchEffect(ViewScope::textField) {
        field.flow.collect { value ->
            display(value)
        }
    }

    return parent
}

private val TextFieldAffixMod = modify(AlignSelfCenter, PaddingX(1), OpacityHalf, PointerEventsNone)

/** The input's start padding beside an icon in units: a unit before the icon, its three, and a unit after it. */
private const val IconInsetUnits = 5

/** The padding that clears an affix of this text beside the input's own padding. */
private fun String.toAffixInset() = LinearDimension("calc(var(--unit) * 2 + ${length}ch)")

/** True when the event is Enter without Shift, outside text composition. */
fun KeyboardEvent.isEnterSubmit() = key == "Enter" && !shiftKey && !isComposing
/** Calls [onEnterSubmit] and prevents the default when the event [isEnterSubmit]. */
fun KeyboardEvent.handleEnterSubmit(onEnterSubmit: () -> Unit) {
    if (isEnterSubmit()) {
        preventDefault()
        onEnterSubmit()
    }
}