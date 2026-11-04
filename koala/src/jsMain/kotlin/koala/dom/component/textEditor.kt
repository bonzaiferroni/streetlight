package koala.dom.component

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.modifier.*
import koala.page.Id
import koala.page.setId
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import kotlinx.html.TEXTAREA
import kotlinx.html.js.div
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.textArea
import org.w3c.dom.HTMLTextAreaElement
import koala.dom.ViewScope
import koala.dom.launchEffect

/** A plain text area of [rows] rows bound to the markdown [state], labeled [label] over its corner. */
fun ViewScope.textEditor(
    state: MutableTap<Markdown>,
    label: String? = null,
    mod: Modifier? = null,
    textMod: Modifier? = null,
    id: Id? = null,
    rows: Int = 5,
    placeholder: String? = label,
    block: TEXTAREA.() -> Unit = {}
) = textEditor(
    text = state.mutableTapOf({ it.value }) { it.toMarkdown() },
    label = label,
    mod = mod,
    textMod = textMod,
    id = id,
    rows = rows,
    placeholder = placeholder,
    block = block,
)

/** A plain text area of [rows] rows bound to [text], labeled [label] over its corner. */
fun ViewScope.textEditor(
    text: MutableTap<String>,
    label: String? = null,
    mod: Modifier? = null,
    textMod: Modifier? = null,
    id: Id? = null,
    rows: Int = 5,
    placeholder: String? = label,
    block: TEXTAREA.() -> Unit = {}
): HTMLTextAreaElement {
    var currentValue = text.now
    lateinit var element: HTMLTextAreaElement

    fun display(value: String) {
        currentValue = value
        if (element.value != value) {
            element.value = value
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
                val newValue = (it.target as HTMLTextAreaElement).value
                if (newValue != currentValue) {
                    text.set(newValue)
                    display(text.now)
                }
            }
            +currentValue
            block()
        }

        launchEffect("textEditor") {
            text.flow.collect {
                display(it)
            }
        }
    }

    return element
}
