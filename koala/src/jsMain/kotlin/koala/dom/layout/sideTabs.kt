package koala.dom.layout

import kampfire.model.Labeled
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import koala.layout.SideTabsStyle
import koala.modifier.*
import kotlinx.html.ButtonType
import kotlinx.html.button
import kotlinx.html.js.div
import web.html.HTMLDivElement
import web.html.HTMLElement
import koala.dom.ViewScope
import koala.dom.asWeb
import koala.dom.launchEffect
import koala.dom.onClick
import koala.dom.querySelectorAll

/** A column of [options] bound to [state], the selected one on a lighter background, beside the [content] it shows. */
fun ViewScope.sideTabs(
    options: List<String>,
    state: MutableTap<String>,
    mod: Modifier? = null,
    flowMod: Modifier? = null,
    content: ViewScope.(String) -> Unit,
): HTMLDivElement {
    var currentValue = state.now

    val element = div {
        addModifiers(modify(SideTabsStyle.Class, mod))
        div {
            addModifiers(SideTabsStyle.Rail)
            attributes["role"] = "tablist"
            attributes["aria-orientation"] = "vertical"
            options.forEach { option ->
                button(type = ButtonType.button) {
                    addModifiers(SideTabsStyle.Tab, TextSmall, TextUppercase)
                    attributes["role"] = "tab"
                    +option
                }
            }
        }
        flowBlock(state, modify(SideTabsStyle.Panel, flowMod)) { content(it) }
    }.asWeb()

    val tabElements = element.querySelectorAll(SideTabsStyle.Tab).map { it as HTMLElement }

    fun display(value: String) {
        currentValue = value
        tabElements.forEachIndexed { index, tab ->
            tab.setAttribute("aria-selected", (options[index] == value).toString())
        }
    }

    tabElements.forEachIndexed { index, tab ->
        tab.onClick {
            if (options[index] != currentValue) state.set(options[index])
        }
    }

    display(currentValue)

    launchEffect("sideTabs") {
        state.flow.collect { display(it) }
    }

    return element
}

/** [sideTabs] of every entry of the enum [E], by label, bound to [state]. */
inline fun <reified E> ViewScope.sideTabs(
    state: MutableTap<E>,
    mod: Modifier? = null,
    flowMod: Modifier? = null,
    crossinline content: ViewScope.(E) -> Unit,
): HTMLDivElement where E : Enum<E>, E : Labeled {
    val enums = enumValues<E>()
    val labels = enums.map { it.label }
    val labelState = state.mutableTapOf({ it.label }) { enums[labels.indexOf(it)] }

    return sideTabs(labels, labelState, mod, flowMod) { content(enums[labels.indexOf(it)]) }
}
