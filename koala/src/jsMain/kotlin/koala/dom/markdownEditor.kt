package koala.dom

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.css.*
import koala.html.Attribute
import koala.html.setAttribute
import koala.model.MutableTap
import koala.model.MarkdownEditorStyle
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLElement

fun ViewScope.markdownEditor(
    state: MutableTap<Markdown>,
    label: String? = null,
    modifiers: ModifierSet? = null,
    placeholder: String? = label,
    block: DIV.() -> Unit = {}
): HTMLElement {
    var currentValue = state.now
    lateinit var element: HTMLElement

    fun display(value: Markdown) {
        currentValue = value
        if (element.innerText != value.value) {
            element.textContent = value.value
        }
    }

    element = div {
        addModifiers(MarkdownEditorStyle.Class, modifiers)
        label?.let {
            setAttribute(Attribute.BlockLabel, label.lowercase())
        }
        setAttribute(Attribute.ContentEditable, "plaintext-only")
        setAttribute(Attribute.Role, "textbox")
        setAttribute(Attribute.AriaMultiline, true)
        label?.let {
            setAttribute(Attribute.AriaLabel , it)
        }
        placeholder?.let {
            setAttribute(Attribute.Placeholder, it)
        }

        onInputFunction = {
            val newValue = element.innerText.toMarkdown()
            if (newValue != currentValue) {
                currentValue = newValue
                state.set(newValue)
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

    return element
}