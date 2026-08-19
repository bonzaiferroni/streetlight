package koala.dom

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.css.*
import koala.html.Attribute
import koala.html.setAttribute
import koala.markdown.markdownSpansOf
import koala.markdown.renderMarkdownSpans
import koala.model.MutableTap
import koala.model.MarkdownEditorStyle
import kotlinx.browser.document
import kotlinx.html.DIV
import kotlinx.html.dom.create
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.p
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get

fun ViewScope.styledMarkdownEditor(
    state: MutableTap<Markdown>,
    label: String? = null,
    modifiers: ModifierSet? = null,
    placeholder: String? = label,
    block: DIV.() -> Unit = {}
): HTMLElement {
    val model = MarkdownEditor()
    var currentValue = Markdown.Empty
    lateinit var element: HTMLElement

    fun display(value: Markdown) {
        if (currentValue == value) return
        currentValue = value
        element.syncFromCollect(model, value)
    }

    element = column {
        addModifiers(MarkdownEditorStyle.Class, modifiers)
        label?.let {
            setAttribute(Attribute.BlockLabel, it.lowercase())
        }
        setAttribute(Attribute.ContentEditable, "plaintext-only")
        setAttribute(Attribute.Role, "textbox")
        setAttribute(Attribute.AriaMultiline, true)
        label?.let {
            setAttribute(Attribute.AriaLabel, it)
        }
        placeholder?.let {
            setAttribute(Attribute.Placeholder, it)
        }

        onInputFunction = {
            val newValue = element.syncFromInput(model)
            if (newValue != currentValue) {
                currentValue = newValue
                state.set(newValue)
            }
        }

        block()
    }

    launchEffect("styledMarkdownEditor") {
        state.flow.collect {
            display(it)
        }
    }

    return element
}

private fun HTMLElement.syncFromCollect(model: MarkdownEditor, markdown: Markdown) {
    val lines = markdown.value.split("\n\n")
    val blocks = model.syncFromCollect(lines)

    lines.forEachIndexed { index, line ->
        val element = children[index] as? HTMLElement
        if (element == null) {
            val lineMod = lineModOf(line)
            val p = document.create.p {
                addModifiers(lineMod)
                val spans = markdownSpansOf(line)
                renderMarkdownSpans(spans, true)
            }
            appendChild(p)
        } else {
            element.syncElement(line)
        }
    }
}

private fun HTMLElement.syncFromInput(model: MarkdownEditor): Markdown {
    console.log("----------- from input")
    val lines = buildList {
        children.asList().toList().forEach { element ->
            val element = element as? HTMLElement ?: return@forEach
            val textContent = element.textContent ?: ""
            val lines = textContent.split('\n')
            lines.forEachIndexed { index, line ->
                println(line)
                add(line)
                val isOriginalElement = index + 1 == lines.size
                when (isOriginalElement) {
                    true -> {
                        element.syncElement(line)
                    }
                    else -> {
                        val lineMod = lineModOf(line)
                        val p = document.create.p {
                            addModifiers(lineMod)
                            val spans = markdownSpansOf(line)
                            renderMarkdownSpans(spans)
                        }
                        insertBefore(p, element)
                    }
                }
            }
        }
    }
    model.linesState.set(lines)
    return lines.joinToString("\n\n").toMarkdown()
}

private fun HTMLElement.syncElement(line: String) {
    val lineMod = lineModOf(line)
    if (textContent != line) {
        console.log("content sync")
        // possible caret work
        textContent = line
    }
    if (!isModified(lineMod)) {
        console.log("setting mod")
        setModifiers(lineMod)
    }
}

private fun lineModOf(line: String): Modifier =
    if (line.trimStart().startsWith("#")) MarkdownEditorStyle.HeadingLine
    else MarkdownEditorStyle.Line
