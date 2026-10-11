package koala.dom.component

import kampfire.model.MutableTap
import koala.modifier.Modifier
import kotlinx.html.INPUT
import kotlinx.html.js.input
import kotlinx.html.js.onInputFunction
import web.html.HTMLElement
import web.html.HTMLInputElement
import koala.dom.ViewScope
import koala.dom.asWeb
import koala.dom.launchEffect
import koala.dom.layout.box
import koala.component.configureSearchFieldContainer
import koala.component.configureSearchFieldInput

/** A [searchField] bound to [state]. */
fun ViewScope.searchField(
    state: MutableTap<String>,
    mod: Modifier? = null,
    textMod: Modifier? = null,
    placeholder: String = "search",
    block: (INPUT.() -> Unit)? = null
): HTMLElement {
    lateinit var element: HTMLInputElement

    val parent = box {
        configureSearchFieldContainer(mod)
        element = input {
            configureSearchFieldInput(textMod, placeholder, state.now)
            onInputFunction = {
                val value = (it.target as HTMLInputElement).value
                if (value != state.now) state.set(value)
            }
            block?.invoke(this)
        }.asWeb()
    }

    launchEffect(ViewScope::searchField) {
        state.flow.collect { value ->
            if (element.value != value) element.value = value
        }
    }

    return parent
}
