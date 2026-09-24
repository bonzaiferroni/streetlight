package koala.dom

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.modifier.*
import koala.html.Id
import koala.html.setId
import kampfire.model.MutableTap
import kotlinx.html.TEXTAREA
import kotlinx.html.js.div
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.textArea
import org.w3c.dom.HTMLTextAreaElement

/** A plain text area of [rows] rows bound to [state], labeled [label] over its corner. */
fun ViewScope.textEditor(
    state: MutableTap<Markdown>,
    label: String? = null,
    mod: Modifier? = null,
    textMod: Modifier? = null,
    id: Id? = null,
    rows: Int = 5,
    placeholder: String? = label,
    block: TEXTAREA.() -> Unit = {}
): HTMLTextAreaElement {
    var currentValue = state.now
    lateinit var element: HTMLTextAreaElement

    fun display(value: Markdown) {
        currentValue = value
        if (element.value != value.value) {
            element.value = value.value
        }
    }

    div {
        addModifiers(mod)
        setAttribute(Attribute.BlockLabel, label?.lowercase())

        element = textArea {
            this.rows = rows.toString()
            addModifiers(Size100P, textMod)
            setId(id)
            label?.let {
                attributes["aria-label"] = it
            }
            placeholder?.let {
                this.placeholder = it
            }
            onInputFunction = {
                val newValue = (it.target as HTMLTextAreaElement).value.toMarkdown()
                if (newValue != currentValue) {
                    state.set(newValue)
                    display(state.now)
                }
            }
            +currentValue.value
            block()
        }

        launchEffect("textEditor") {
            state.flow.collect {
                display(it)
            }
        }
    }

    return element
}