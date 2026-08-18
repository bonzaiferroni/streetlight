package koala.dom

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.css.ModifierSet
import koala.css.Size100P
import koala.css.addModifiers
import koala.html.Id
import koala.html.Attribute
import koala.html.setId
import koala.html.setAttribute
import koala.model.MutableTap
import kotlinx.html.TEXTAREA
import kotlinx.html.js.div
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.textArea
import org.w3c.dom.HTMLTextAreaElement

fun ViewScope.textEditor(
    state: MutableTap<Markdown>,
    label: String? = null,
    modifiers: ModifierSet? = null,
    textModifiers: ModifierSet? = null,
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
        addModifiers(modifiers)
        setAttribute(Attribute.BlockLabel, label?.lowercase())

        element = textArea {
            this.rows = rows.toString()
            addModifiers(Size100P, textModifiers)
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