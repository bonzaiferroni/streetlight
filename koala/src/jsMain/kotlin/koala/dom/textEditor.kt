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
import koala.model.MutableField
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.TEXTAREA
import kotlinx.html.js.div
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.textArea
import org.w3c.dom.HTMLTextAreaElement

fun ViewScope.textEditor(
    field: MutableField<Markdown>,
    label: String? = null,
    modifiers: ModifierSet? = null,
    textModifiers: ModifierSet? = null,
    id: Id? = null,
    rows: Int = 5,
    placeholder: String? = label,
    block: TEXTAREA.() -> Unit = {}
): HTMLTextAreaElement {
    var currentValue = field.now
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
                    field.set(newValue)
                    display(field.now)
                }
            }
            +currentValue.value
            block()
        }

        launchEffect("textEditor") {
            field.flow.collect {
                display(it)
            }
        }
    }

    return element
}