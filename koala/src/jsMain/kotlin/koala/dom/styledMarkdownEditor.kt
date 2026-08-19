package koala.dom

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.css.*
import koala.html.Attribute
import koala.html.setAttribute
import koala.markdown.MarkdownBlock
import koala.markdown.MarkdownBlockType
import koala.markdown.markdownBlockTypeOf
import koala.markdown.markdownBlocksOf
import koala.markdown.markdownSpansOf
import koala.markdown.renderMarkdownSpans
import koala.model.MutableTap
import koala.model.MarkdownEditorStyle
import kotlinx.browser.document
import kotlinx.dom.clear
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

    launchEffect("styledMarkdownEditor") {
        state.flow.collect {
            display(it)
        }
    }

    return element
}

private fun HTMLElement.syncFromCollect(model: MarkdownEditor, markdown: Markdown) {
    val chunks = markdown.value.split("\n\n")
    val blocks = model.syncFromCollect(chunks)

    chunks.forEachIndexed { index, chunk ->
        val element = children[index] as? HTMLElement
        val block = blocks[index]
        if (element == null) {
            val lineMod = chunkModOf(block?.blockType)
            val p = document.create.p {
                addModifiers(lineMod)
                +chunk
            }
            appendChild(p)
        } else {
            element.syncElement(chunk, block)
        }
    }
}

private fun HTMLElement.syncFromInput(model: MarkdownEditor): Markdown {
    console.log("----------- from input")
    val blocks = mutableListOf<MarkdownBlock?>()
    val chunks = mutableListOf<String>()
    children.asList().toList().forEach { element ->
        val element = element as? HTMLElement ?: return@forEach
        val elementChunk = element.textContent ?: ""
        val cachedBlock = model.getCachedBlockOrNull(elementChunk)
        if (cachedBlock != null) {
            element.syncChunkMod(cachedBlock.blockType)
            chunks.add(elementChunk)
            blocks.add(cachedBlock)
            return@forEach
        }

        // val blocks = markdownBlocksOf(elementChunk.toMarkdown())
        // blocks.forEachIndexed { index, block ->
        //     val isOriginalElement = index + 1 == blocks.size
        //     when (isOriginalElement) {
        //         true -> {
        //             element.syncElement(text, block)
        //         }
        //     }
        // }

        val splitChunks = elementChunk.split("\n\n")
        console.log("chunks: ${splitChunks.size}")
        splitChunks.forEachIndexed { index, chunk ->
            val block = markdownBlocksOf(chunk.toMarkdown()).firstOrNull() ?: return@forEachIndexed
            chunks.add(chunk)
            blocks.add(block)
            val isOriginalElement = index + 1 == splitChunks.size
            when (isOriginalElement) {
                true -> {
                    element.syncElement(chunk, block)
                }
                else -> {
                    val blockType = markdownBlockTypeOf(chunk)
                    println("chunk: $chunk")
                    val lineMod = chunkModOf(blockType)
                    val p = document.create.p {
                        addModifiers(lineMod)
                        val spans = markdownSpansOf(chunk)
                        renderMarkdownSpans(spans)
                    }
                    insertBefore(p, element)
                }
            }
        }
    }
    model.syncFromInput(chunks, blocks)
    return chunks.joinToString("\n\n").toMarkdown()
}

private fun HTMLElement.syncElement(chunk: String, block: MarkdownBlock?) {
    val blockType = block?.blockType
    if (textContent != chunk) {
        console.log("content sync")
        // possible caret work
        textContent = chunk
    }
    syncChunkMod(blockType)
}

private fun HTMLElement.syncChunkMod(blockType: MarkdownBlockType?) {
    val chunkMod = chunkModOf(blockType)
    if (!isModified(chunkMod)) {
        console.log("setting mod")
        setModifiers(chunkMod)
    }
}

private fun chunkModOf(blockType: MarkdownBlockType?): Modifier = blockType?.let {
    when (blockType) {
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
} ?: MarkdownEditorStyle.Chunk
