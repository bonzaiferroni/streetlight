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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.html.TEXTAREA
import kotlinx.html.js.div
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.textArea
import org.w3c.dom.HTMLTextAreaElement

fun ViewScope.textEditor(
    label: String? = null,
    modifiers: ModifierSet? = null,
    textModifiers: ModifierSet? = null,
    id: Id? = null,
    onValue: ((Markdown) -> Unit)? = null,
    flow: Flow<Markdown?>? = null,
    rows: Int = 5,
    placeholder: String? = label,
    block: TEXTAREA.() -> Unit = {}
): HTMLTextAreaElement {
    var currentValue = ""
    var textElement: HTMLTextAreaElement? = null

    div {
        addModifiers(modifiers)
        setAttribute(Attribute.BlockLabel, label)
        textElement = configureTextEditor(
            label = label,
            textModifiers = textModifiers,
            id = id,
            onValue = { value ->
                if (value != currentValue) {
                    currentValue = value
                    onValue?.invoke(value.toMarkdown())
                }
            },
            rows = rows,
            placeholder = placeholder,
            block = block
        )
    }

    flow?.let {
        scope.launch {
            flow.collect { value ->
                val value = value?.value ?: ""
                if (value != currentValue) {
                    currentValue = value
                    textElement!!.value = value
                }
            }
        }
    }

    return textElement!!
}

fun TagScope.configureTextEditor(
    label: String? = null,
    textModifiers: ModifierSet? = null,
    id: Id? = null,
    onValue: ((String) -> Unit)? = null,
    rows: Int = 5,
    placeholder: String? = label,
    block: TEXTAREA.() -> Unit = {}
) = textArea {
    this.rows = rows.toString()
    addModifiers(Size100P, textModifiers)
    setId(id)
    label?.let {
        attributes["aria-label"] = it
    }
    placeholder?.let {
        this.placeholder = it
    }

    onValue?.let { callback ->
        onInputFunction = {
            val value = (it.target as HTMLTextAreaElement).value
            callback.invoke(value)
        }
    }
    block()
}

//fun DOMContext.domTextEditor(
//    label: String? = null,
//    modifiers: ModifierSet? = null,
//    textModifiers: ModifierSet? = null,
//    id: Id? = null,
//    onValue: ((String) -> Unit)? = null,
//    rows: Int = 5,
//    placeholder: String? = label,
//    block: TEXTAREA.() -> Unit = {}
//): HTMLTextAreaElement {
//    var currentValue = ""
//    var textElement: HTMLTextAreaElement? = null
//    div {
//        addModifiers(modifiers)
//        setAttribute(Attribute.BlockLabel, label)
//        textElement = configureTextEditor(
//            label = label,
//            textModifiers = textModifiers,
//            id = id,
//            onValue = { value ->
//                if (value != currentValue) {
//                    currentValue = value
//                    onValue?.invoke(value)
//                }
//            },
//            rows = rows,
//            placeholder = placeholder,
//            block = block
//        )
//    }
//    return textElement!!
//}