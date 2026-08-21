package koala.dom

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.markdown.ContentType
import koala.markdown.MarkdownParser
import koala.markdown.MarkdownRegex
import koala.markdown.ParsedBlock
import koala.markdown.accepts
import koala.markdown.markdownBlockTypeOf
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

class MarkdownInputParser(private val model: MarkdownEditor) {

    private val blocks = mutableListOf<ParsedBlock>()
    private val produced = mutableListOf<Pair<HTMLElement, String>>()
    private val caretTargets = mutableListOf<Pair<HTMLElement, String>>()
    private val pending = PendingChunk()
    private val parser = MarkdownParser()

    private var activeElement: HTMLElement? = null
    private var activeOffset: Int? = null
    private var caretTargetOffset: Int? = null

    fun syncFromInput(container: HTMLElement): Markdown {
        reset()
        activeElement = container.activeChunk()
        activeOffset = activeElement?.caretOffset()

        container.children.asList().toList().forEach { child ->
            val childElement = child as? HTMLElement ?: return@forEach
            val childChunk = childElement.normalizedTextContent()
            val childOffset = if (childElement === activeElement) activeOffset else null

            if (pending.isOpen && pending.accepts(childChunk)) {
                pending.absorb(childChunk, childOffset)
                container.removeChild(childElement)
                return@forEach
            }

            commit(container)
            pending.open(childElement, childChunk, childOffset)
        }
        commit(container)

        model.syncFromInput(blocks.toList())
        placeCaret()

        return blocks.joinToString("\n") { it.chunk }.toMarkdown()
    }

    private fun reset() {
        blocks.clear()
        caretTargets.clear()
        caretTargetOffset = null
        pending.close()
    }

    private fun commit(container: HTMLElement) {
        val element = pending.close() ?: return
        val chunk = pending.chunk
        val caretOffset = pending.caretOffset
        produced.clear()

        val cachedBlock = model.getCachedBlockOrNull(chunk)
        if (cachedBlock != null) {
            element.syncBlockMod(cachedBlock.markdown)
            blocks.add(cachedBlock)
            produced.add(element to chunk)
            markCaret(caretOffset)
            return
        }

        // console.log("text: ${JSON.stringify(element.textContent)}")
        // console.log("inner: ${JSON.stringify(element.innerText)}")
        // console.log("html: ${element.innerHTML}")

        val parsed = parser.parseBlocks(chunk, true)
        parsed.forEachIndexed { index, block ->
            blocks.add(block)
            val isOriginalElement = index + 1 == parsed.size
            when (isOriginalElement) {
                true -> {
                    element.syncMarkdownElement(block)
                    produced.add(element to block.chunk)
                }
                else -> {
                    val p = createMarkdownElement(block)
                    container.insertBefore(p, element)
                    produced.add(p to block.chunk)
                }
            }
        }
        markCaret(caretOffset)
    }

    private fun markCaret(offset: Int?) {
        offset ?: return
        caretTargets.clear()
        caretTargets.addAll(produced)
        caretTargetOffset = offset
    }

    private fun placeCaret() {
        val offset = caretTargetOffset ?: return
        placeCaretAcross(caretTargets, offset)
    }
}

private class PendingChunk {
    var element: HTMLElement? = null
        private set
    var chunk = ""
        private set
    var caretOffset: Int? = null
        private set

    private var type: ContentType? = null
    private var openFence = false

    val isOpen get() = element != null

    fun open(element: HTMLElement, chunk: String, caretOffset: Int?) {
        this.element = element
        this.caretOffset = caretOffset
        setChunk(chunk)
    }

    fun absorb(chunk: String, caretOffset: Int?) {
        caretOffset?.let { this.caretOffset = this.chunk.length + 1 + it }
        setChunk("${this.chunk}\n$chunk")
    }

    fun accepts(nextChunk: String): Boolean {
        val type = type ?: return false
        if (type == ContentType.Code) return openFence
        return type.accepts(nextChunk.substringBefore('\n'))
    }

    fun close(): HTMLElement? {
        val element = element ?: return null
        this.element = null
        return element
    }

    private fun setChunk(chunk: String) {
        this.chunk = chunk
        type = markdownBlockTypeOf(chunk.substringBefore('\n'))
        openFence = type == ContentType.Code && chunk.hasOpenFence()
    }
}

private fun String.hasOpenFence(): Boolean =
    lineSequence().count { MarkdownRegex.Fence.matches(it) } % 2 == 1