package koala.dom.markdown

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.modifier.*
import kampfire.model.MutableTap
import kampfire.model.mutableTapOf
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLElement
import koala.dom.ViewScope
import koala.dom.launchEffect
import koala.markdown.EditorStyle

/** [basicEditor] bound to the markdown [state]. */
fun ViewScope.basicEditor(
    state: MutableTap<Markdown>,
    label: String? = null,
    mod: Modifier? = null,
    placeholder: String? = label,
    spellcheck: Boolean = true,
    block: DIV.() -> Unit = {}
) = basicEditor(
    text = state.mutableTapOf({ it.value }) { it.toMarkdown() },
    label = label,
    mod = mod,
    placeholder = placeholder,
    spellcheck = spellcheck,
    block = block,
)

/**
 * An editable plain-text area bound to [text], labeled [label] over its corner, as tall as its lines. With
 * [spellcheck], the browser marks words it does not know.
 *
 * Typing writes to [text], and a change to [text] from elsewhere replaces the text.
 */
fun ViewScope.basicEditor(
    text: MutableTap<String>,
    label: String? = null,
    mod: Modifier? = null,
    placeholder: String? = label,
    spellcheck: Boolean = true,
    block: DIV.() -> Unit = {}
): HTMLElement {
    var currentValue = text.now
    lateinit var element: HTMLElement

    fun display(value: String) {
        if (currentValue == value) return
        currentValue = value
        element.textContent = value
    }

    element = div {
        addModifiers(EditorStyle.Container, mod)
        label?.let {
            setAttribute(Attribute.BlockLabel, label.lowercase())
        }
        setAttribute(Attribute.ContentEditable, "plaintext-only")
        setAttribute(Attribute.Role, "textbox")
        setAttribute(Attribute.AriaMultiline, true)
        setAttribute(Attribute.Spellcheck, spellcheck)
        label?.let {
            setAttribute(Attribute.AriaLabel , it)
        }
        placeholder?.let {
            setAttribute(Attribute.Placeholder, it)
        }

        onInputFunction = {
            val newValue = element.innerText
            if (newValue != currentValue) {
                currentValue = newValue
                text.set(newValue)
            }
        }

        +currentValue
        block()
    }

    launchEffect("basicEditor") {
        text.flow.collect {
            display(it)
        }
    }

    return element
}
