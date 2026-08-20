package koala.dom

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.markdown.MarkdownBlockType
import koala.markdown.ParsedBlock
import koala.markdown.accepts
import koala.markdown.markdownBlockTypeOf
import koala.markdown.markdownBlocksOf
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

class MarkdownInputParser(private val model: MarkdownEditor) {

    private val blocks = mutableListOf<ParsedBlock>()
    private val produced = mutableListOf<Pair<HTMLElement, String>>()
    private val caretTargets = mutableListOf<Pair<HTMLElement, String>>()

    private var activeElement: HTMLElement? = null
    private var activeOffset: Int? = null
    private var caretTargetOffset: Int? = null

    private var pendingElement: HTMLElement? = null
    private var pendingChunk = ""
    private var pendingOffset: Int? = null

    fun syncFromInput(container: HTMLElement): Markdown {
        reset()
        activeElement = container.activeChunk()
        activeOffset = activeElement?.caretOffset()

        container.children.asList().toList().forEach { child ->
            val childElement = child as? HTMLElement ?: return@forEach
            val childChunk = childElement.normalizedTextContent()

            if (pendingElement != null && mergesWith(pendingChunk, childChunk)) {
                absorb(container, childElement, childChunk)
                return@forEach
            }

            commit(container)
            pendingElement = childElement
            pendingChunk = childChunk
            pendingOffset = if (childElement === activeElement) activeOffset else null
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
        pendingElement = null
        pendingChunk = ""
        pendingOffset = null
    }

    private fun absorb(container: HTMLElement, childElement: HTMLElement, childChunk: String) {
        if (childElement === activeElement) {
            pendingOffset = pendingChunk.length + 1 + (activeOffset ?: 0)
        }
        pendingChunk = "$pendingChunk\n$childChunk"
        container.removeChild(childElement)
    }

    private fun commit(container: HTMLElement) {
        val element = pendingElement ?: return
        pendingElement = null

        val cachedBlock = model.getCachedBlockOrNull(pendingChunk)
        if (cachedBlock != null) {
            element.syncChunkMod(cachedBlock.markdown.blockType)
            blocks.add(cachedBlock)
            pendingOffset?.let { markCaretTarget(it, element, pendingChunk) }
            return
        }

        // console.log("text: ${JSON.stringify(element.textContent)}")
        // console.log("inner: ${JSON.stringify(element.innerText)}")
        // console.log("html: ${element.innerHTML}")

        val parsed = markdownBlocksOf(pendingChunk.toMarkdown(), true)
        produced.clear()
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
        pendingOffset?.let { markCaretTargets(it) }
    }

    private fun markCaretTarget(offset: Int, element: HTMLElement, chunk: String) {
        caretTargets.clear()
        caretTargets.add(element to chunk)
        caretTargetOffset = offset
    }

    private fun markCaretTargets(offset: Int) {
        caretTargets.clear()
        caretTargets.addAll(produced)
        caretTargetOffset = offset
    }

    private fun placeCaret() {
        val offset = caretTargetOffset ?: return
        placeCaretAcross(caretTargets, offset)
    }

    private fun mergesWith(previousChunk: String, nextChunk: String): Boolean {
        if (previousChunk.isEmpty() || nextChunk.isEmpty()) return false
        val type = markdownBlockTypeOf(previousChunk.substringBefore('\n')) ?: return false
        if (type == MarkdownBlockType.Code) return false
        return type.accepts(nextChunk.substringBefore('\n'))
    }
}