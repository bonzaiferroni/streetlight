package koala.dom

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.markdown.MarkdownBlock
import koala.markdown.MarkdownParagraph
import koala.markdown.ParsedBlock
import koala.markdown.markdownBlocksOf
import koala.model.mutableTapOf
import koala.model.storeOf

class MarkdownEditor() {
    private val state = storeOf(MarkdownEditorState())

    val blocksState = state.mutableTapOf({ it.blocks }) { copy(blocks = it) }

    fun getCachedBlockOrNull(chunk: String): ParsedBlock? {
        return state.now.blocks.firstOrNull { it.chunk == chunk }
    }

    fun syncFromInput(blocks: List<ParsedBlock>) {
        state.set { copy(blocks = blocks) }
    }

    fun syncFromCollect(markdown: Markdown): List<ParsedBlock> {
        val blocks = markdownBlocksOf(markdown, true)
        state.set { copy(blocks = blocks) }
        return blocks
    }
}

data class MarkdownEditorState(
    val blocks: List<ParsedBlock> = emptyList()
)