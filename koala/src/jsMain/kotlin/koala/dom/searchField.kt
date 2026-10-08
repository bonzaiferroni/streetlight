package koala.dom

import kampfire.model.MutableTap
import koala.html.configureSearchFieldContainer
import koala.html.configureSearchFieldInput
import koala.modifier.Modifier
import kotlinx.html.INPUT
import kotlinx.html.js.input
import kotlinx.html.js.onInputFunction
import web.html.HTMLElement
import web.html.HTMLInputElement

/** A [koala.html.searchField] bound to [field]. */
fun ViewScope.searchField(
    field: MutableTap<String>,
    mod: Modifier? = null,
    textMod: Modifier? = null,
    placeholder: String = "search",
    block: (INPUT.() -> Unit)? = null
): HTMLElement {
    lateinit var element: HTMLInputElement

    val parent = box {
        configureSearchFieldContainer(mod)
        element = input {
            configureSearchFieldInput(textMod, placeholder, field.now)
            onInputFunction = {
                val value = (it.target as HTMLInputElement).value
                if (value != field.now) field.set(value)
            }
            block?.invoke(this)
        }.asWeb()
    }

    launchEffect(ViewScope::searchField) {
        field.flow.collect { value ->
            if (element.value != value) element.value = value
        }
    }

    return parent
}
