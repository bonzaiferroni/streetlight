package koala.dom

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.css.*
import koala.external.selection
import koala.html.Attribute
import koala.html.setAttribute
import koala.html.span
import koala.markdown.MarkdownBlock
import koala.markdown.MarkdownBlockType
import koala.markdown.ParsedBlock
import koala.markdown.markdownBlocksOf
import koala.model.MutableTap
import koala.model.MarkdownEditorStyle
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.clear
import kotlinx.html.DIV
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.onInputFunction
import kotlinx.html.js.p
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
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
    var currentValue: Markdown? = null
    lateinit var element: HTMLElement

    fun display(value: Markdown) {
        if (currentValue == value) return
        currentValue = value
        element.syncFromCollect(model, value)
    }

    element = column(modify(Gap0)) {
        addModifiers(MarkdownEditorStyle.Container, modifiers)
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
            val p = createMarkdownElement(block)
            appendChild(p)
        } else {
            if (element.normalizedTextContent() == block.chunk) return@forEachIndexed
            element.syncMarkdownElement(block)
        }
    }
}

private fun HTMLElement.syncFromInput(model: MarkdownEditor): Markdown {
    val activeElement = activeChunk()
    val caretOffset = activeElement?.caretOffset()
    var caretTargets: List<Pair<HTMLElement, String>>? = null

    val blocks = buildList {
        children.asList().toList().forEach { element ->
            val element = element as? HTMLElement ?: return@forEach

            val elementChunk = element.normalizedTextContent()
            val cachedBlock = model.getCachedBlockOrNull(elementChunk)
            if (cachedBlock != null) {
                element.syncChunkMod(cachedBlock.markdown.blockType)
                add(cachedBlock)
                return@forEach
            }

            // console.log("text: ${JSON.stringify(element.textContent)}")
            // console.log("inner: ${JSON.stringify(element.innerText)}")
            // console.log("html: ${element.innerHTML}")

            val parsed = markdownBlocksOf(elementChunk.toMarkdown(), true)
            val produced = mutableListOf<Pair<HTMLElement, String>>()
            parsed.forEachIndexed { index, block ->
                add(block)
                val isOriginalElement = index + 1 == parsed.size
                when (isOriginalElement) {
                    true -> {
                        element.syncMarkdownElement(block)
                        produced.add(element to block.chunk)
                    }
                    else -> {
                        val p = createMarkdownElement(block)
                        insertBefore(p, element)
                        produced.add(p to block.chunk)
                    }
                }
            }
            if (element === activeElement) caretTargets = produced
        }
    }

    model.syncFromInput(blocks)

    caretOffset?.let { offset ->
        caretTargets?.let { placeCaretAcross(it, offset) }
    }

    return blocks.joinToString("\n") { it.chunk }.toMarkdown()
}

private fun createMarkdownElement(block: ParsedBlock): HTMLElement {
    val (chunk, markdown) = block
    val chunkMod = chunkModOf(markdown.blockType)
    val p = document.create.p {
        addModifiers(chunkMod)
    }
    p.append {
        renderEditorBlock(block)
    }
    return p
}

private fun HTMLElement.syncMarkdownElement(block: ParsedBlock) {
    syncChunkMod(block.markdown.blockType)
    clear()
    append {
        renderEditorBlock(block)
    }
}

private fun HTMLElement.syncChunkMod(blockType: MarkdownBlockType) {
    val chunkMod = chunkModOf(blockType)
    if (!isModified(chunkMod)) {
        // console.log("setting mod")
        setModifiers(chunkMod)
    }
}

private fun chunkModOf(blockType: MarkdownBlockType): Modifier = when (blockType) {
    MarkdownBlockType.Image -> MarkdownEditorStyle.BlockImage
    MarkdownBlockType.Paragraph -> MarkdownEditorStyle.Paragraph
    MarkdownBlockType.Heading -> MarkdownEditorStyle.Heading
    MarkdownBlockType.HorizontalRule -> MarkdownEditorStyle.HorizontalRule
    MarkdownBlockType.Code -> MarkdownEditorStyle.Code
    MarkdownBlockType.BlockQuote -> MarkdownEditorStyle.BlockQuote
    MarkdownBlockType.UnorderedList -> MarkdownEditorStyle.UnorderedList
    MarkdownBlockType.OrderedList -> MarkdownEditorStyle.OrderedList
    MarkdownBlockType.Table -> MarkdownEditorStyle.Table
}

fun HTMLElement.caretOffset(): Int? {
    val selection = window.selection() ?: return null
    if (selection.rangeCount == 0) return null

    val range = selection.getRangeAt(0)
    if (!contains(range.startContainer)) return null

    val probe = range.cloneRange()
    probe.selectNodeContents(this)
    probe.setEnd(range.startContainer, range.startOffset)
    return probe.toString().length
}

fun HTMLElement.activeChunk(): HTMLElement? {
    val selection = window.selection() ?: return null
    if (selection.rangeCount == 0) return null

    var node: Node? = selection.getRangeAt(0).startContainer
    while (node != null && node.parentNode != this) {
        node = node.parentNode
    }
    return node as? HTMLElement
}