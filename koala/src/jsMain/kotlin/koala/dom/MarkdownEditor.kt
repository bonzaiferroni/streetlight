package koala.dom

import kampfire.api.toMarkdown
import koala.markdown.MarkdownBlock
import koala.markdown.markdownBlocksOf
import koala.model.mutableTapOf
import koala.model.storeOf

class MarkdownEditor() {
    private val state = storeOf(MarkdownEditorState())

    val linesState = state.mutableTapOf({ it.chunks }) { copy(chunks = it) }
    val blocksState = state.mutableTapOf({ it.blocks }) { copy(blocks = it) }

    fun getCachedBlockOrNull(chunk: String): MarkdownBlock? {
        return state.now.chunks.indexOf(chunk).takeIf { it >= 0 }?.let {
            state.now.blocks[it]
        } // ?: markdownBlocksOf(line.toMarkdown()).firstOrNull()
    }

    fun syncFromInput(chunks: List<String>, blocks: List<MarkdownBlock?>) {
        state.set { copy(chunks = chunks, blocks = blocks) }
    }

    fun syncFromCollect(chunks: List<String>): List<MarkdownBlock?> {
        val previousLines = linesState.now
        val previousBlocks = blocksState.now
        val reusable by lazy { previousLines.zip(previousBlocks).toMap() }

        val blocks = chunks.mapIndexed { index, line ->
            when {
                previousLines.getOrNull(index) == line -> previousBlocks.getOrNull(index)
                else -> reusable[line] ?: markdownBlocksOf(line.toMarkdown()).firstOrNull()
            }
        }

        state.set { copy(chunks = chunks, blocks = blocks) }
        return blocks
    }
}

data class MarkdownEditorState(
    val chunks: List<String> = emptyList(),
    val blocks: List<MarkdownBlock?> = emptyList()
)