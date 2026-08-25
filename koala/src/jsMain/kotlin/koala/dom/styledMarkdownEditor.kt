package koala.dom

import kampfire.api.Markdown
import koala.css.*
import koala.html.Attribute
import koala.html.setAttribute
import koala.model.MutableTap
import koala.model.EditorStyle
import kotlinx.browser.document
import kotlinx.html.DIV
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

fun ViewScope.styledMarkdownEditor(
    state: MutableTap<Markdown>,
    label: String? = null,
    mod: ModifierSet? = null,
    placeholder: String? = label,
    block: DIV.() -> Unit = {}
): HTMLElement {
    val model = MarkdownEditor()
    val inputParser = MarkdownInputParser(model)
    var currentValue: Markdown? = null
    lateinit var element: HTMLElement

    fun display(value: Markdown) {
        if (currentValue == value) return
        currentValue = value
        element.syncFromCollect(model, value)
    }

    element = column(modify(Gap0)) {
        addModifiers(EditorStyle.Container, mod)
        label?.let {
            setAttribute(Attribute.BlockLabel, it.lowercase())
        }
        setAttribute(Attribute.ContentEditable, "plaintext-only")
        setAttribute(Attribute.Role, "textbox")
        setAttribute(Attribute.AriaMultiline, true)
        setAttribute(Attribute.Spellcheck, true)
        label?.let {
            setAttribute(Attribute.AriaLabel, it)
        }
        placeholder?.let {
            setAttribute(Attribute.Placeholder, it)
        }

        onInputFunction = {
            val newValue = inputParser.syncFromInput(element)
            if (newValue != currentValue) {
                currentValue = newValue
                state.set(newValue)
            }
        }

        block()
    }

//    element.addEventListener("copy", { event ->
//        val range = window.selection()?.getRangeAt(0) ?: return@addEventListener
//        val fragment = range.cloneContents()
//        val text = range.cloneContents().childNodes.asList()
//            .joinToString("\n") { it.textContent ?: "" }
//        event.unsafeCast<ClipboardEvent>().clipboardData?.setData("text/plain", text)
//        event.preventDefault()
//    })

    document.addEventListener("selectionchange", {
        element.activeChunk()?.let {
            // console.log("offset ${it.caretOffset()} in ${it.className}")
        }
    })

    launchEffect("styledMarkdownEditor") {
        state.flow.collect {
            display(it)
        }
    }

    return element
}

private fun HTMLElement.syncFromCollect(model: MarkdownEditor, markdown: Markdown) {
    val blocks = model.syncFromCollect(markdown)

    blocks.forEachIndexed { index, block ->
        val element = children[index] as? HTMLElement
        if (element == null) {
            val p = createEditorBlock(block)
            appendChild(p)
        } else {
            if (element.normalizedTextContent() == block.chunk) return@forEachIndexed
            element.syncEditorBlock(block)
        }
    }
}

