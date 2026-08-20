package koala.dom

import kampfire.api.toMarkdown
import koala.markdown.MarkdownBlock
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

    fun syncFromCollect(chunks: List<String>): List<ParsedBlock> {
        val blocks = chunks.map { chunk ->
            getCachedBlockOrNull(chunk)
                ?: markdownBlocksOf(chunk.toMarkdown()).firstOrNull()
                ?: ParsedBlock(chunk, null)
        }
        state.set { copy(blocks = blocks) }
        return blocks
    }
}

data class MarkdownEditorState(
    val blocks: List<ParsedBlock> = emptyList()
)