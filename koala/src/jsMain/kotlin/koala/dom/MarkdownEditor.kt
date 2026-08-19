package koala.dom

import kampfire.api.toMarkdown
import koala.markdown.MarkdownBlock
import koala.markdown.markdownBlocksOf
import koala.model.mutableTapOf
import koala.model.storeOf

class MarkdownEditor() {
    private val state = storeOf(MarkdownEditorState())

    val linesState = state.mutableTapOf({ it.lines }) { copy(lines = it) }
    val blocksState = state.mutableTapOf({ it.blocks }) { copy(blocks = it) }

    fun getBlock(line: String): MarkdownBlock? {
        return state.now.lines.indexOf(line).takeIf { it >= 0 }?.let {
            state.now.blocks[it]
        } ?: markdownBlocksOf(line.toMarkdown()).firstOrNull()
    }

    fun syncFromInput(lines: List<String>, blocks: List<MarkdownBlock?>) {
        state.set { copy(lines = lines, blocks = blocks) }
    }

    fun syncFromCollect(lines: List<String>): List<MarkdownBlock?> {
        val previousLines = linesState.now
        val previousBlocks = blocksState.now
        val reusable by lazy { previousLines.zip(previousBlocks).toMap() }

        val blocks = lines.mapIndexed { index, line ->
            when {
                previousLines.getOrNull(index) == line -> previousBlocks.getOrNull(index)
                else -> reusable[line] ?: markdownBlocksOf(line.toMarkdown()).firstOrNull()
            }
        }

        state.set { copy(lines = lines, blocks = blocks) }
        return blocks
    }
}

data class MarkdownEditorState(
    val lines: List<String> = emptyList(),
    val blocks: List<MarkdownBlock?> = emptyList()
)