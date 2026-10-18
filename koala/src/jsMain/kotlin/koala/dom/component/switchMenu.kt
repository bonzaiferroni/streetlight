package koala.dom.component

import kampfire.model.Labeled
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import koala.component.SwitchMenuStyle
import koala.modifier.*
import kotlinx.coroutines.await
import kotlinx.css.LinearDimension
import kotlinx.css.px
import kotlinx.html.ButtonType
import kotlinx.html.button
import kotlinx.html.js.div
import kotlinx.html.span
import web.dom.document
import web.html.HTMLDivElement
import web.html.HTMLElement
import koala.dom.ViewScope
import koala.dom.asWeb
import koala.dom.launchEffect
import koala.dom.onClick
import koala.dom.querySelector
import koala.dom.querySelectorAll
import kotlin.js.Promise

/** A row of [options] bound to [state], the selected option lighted, wrapping onto more rows as needed. */
fun ViewScope.switchMenu(
    options: List<String>,
    state: MutableTap<String>,
    mod: Modifier? = null,
): HTMLDivElement {
    var currentValue = state.now

    val element = div {
        addModifiers(modify(SwitchMenuStyle.Class, Outline, mod))
        attributes["role"] = "radiogroup"
        options.forEach { option ->
            button(type = ButtonType.button) {
                addModifiers(SwitchMenuStyle.Option)
                attributes["role"] = "radio"
                span {
                    addModifiers(SwitchMenuStyle.Label)
                    +option
                }
            }
        }
    }.asWeb()

    val optionElements = element.querySelectorAll(SwitchMenuStyle.Option).map { it as HTMLElement }

    fun display(value: String) {
        currentValue = value
        optionElements.forEachIndexed { index, option ->
            option.setAttribute("aria-checked", (options[index] == value).toString())
        }
    }

    optionElements.forEachIndexed { index, option ->
        option.onClick {
            if (options[index] != currentValue) state.set(options[index])
        }
    }

    display(currentValue)

    launchEffect("switchMenu") {
        state.flow.collect { display(it) }
    }

    // the widest label sets the width every option shares, measured once the fonts are in
    launchEffect("switchMenu > width") {
        document.asDynamic().fonts.ready.unsafeCast<Promise<Any?>>().await()
        val widest = optionElements.maxOfOrNull { it.querySelector(SwitchMenuStyle.Label)?.offsetWidth ?: 0 } ?: 0
        element.setStyle(SwitchMenuStyle.OptionWidth.of(LinearDimension("calc(${widest.px} + 2rem)")))
    }

    return element
}

/** A row of every entry of the enum [E], by label, bound to [state]. */
inline fun <reified E> ViewScope.switchMenu(
    state: MutableTap<E>,
    mod: Modifier? = null,
): HTMLDivElement where E : Enum<E>, E : Labeled {
    val enums = enumValues<E>()
    val values = enums.map { it.label }
    val textField = state.mutableTapOf({ it.label }) { enums[values.indexOf(it)] }

    return switchMenu(values, textField, mod)
}
