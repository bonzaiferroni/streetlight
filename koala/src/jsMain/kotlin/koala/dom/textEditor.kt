package koala.dom

import koala.css.ModifierSet
import koala.css.Width100P
import koala.css.addModifiers
import koala.html.Id
import koala.html.TagAttribute
import koala.html.setId
import koala.html.setAttribute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.TEXTAREA
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.textArea
import org.w3c.dom.HTMLTextAreaElement

fun RenderContext.textEditor(
    label: String? = null,
    modifiers: ModifierSet? = null,
    textModifiers: ModifierSet? = null,
    id: Id? = null,
    onChangeValue: ((String) -> Unit)? = null,
    binding: Flow<String>? = null,
    rows: Int = 5,
    placeholder: String? = null,
    block: (TEXTAREA.() -> Unit)? = null
): HTMLTextAreaElement {
    val parent = div {
        addModifiers(modifiers)
        setAttribute(TagAttribute.blockLabel, label)
    }

    var currentValue = ""
    val element = parent.append {
        textArea {
            this.rows = rows.toString()
            addModifiers(Width100P, textModifiers)
            setId(id)
            label?.let {
                attributes["aria-label"] = it
            }
            placeholder?.let {
                this.placeholder = it
            }

            onChangeValue?.let { callback ->
                onInputFunction = {
                    val value = (it.target as HTMLTextAreaElement).value
                    if (value != currentValue) {
                        currentValue = value
                        callback(value)
                    }
                }
            }
            block?.invoke(this)
        }
    }.first() as HTMLTextAreaElement

    binding?.let {
        renderScope.launch {
            binding.collect { value ->
                if (value != currentValue) {
                    currentValue = value
                    element.value = value
                }
            }
        }
    }

    return element
}