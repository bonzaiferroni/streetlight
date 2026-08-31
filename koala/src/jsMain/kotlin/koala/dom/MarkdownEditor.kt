package koala.dom

import kampfire.api.Markdown
import koala.markdown.MarkdownParser
import koala.markdown.ParsedBlock
import kampfire.model.mutableTapOf
import kampfire.model.storeOf

class MarkdownEditor() {
    private val state = storeOf(MarkdownEditorState())
    private val parser = MarkdownParser()

    val blocksState = state.mutableTapOf({ it.blocks }) { copy(blocks = it) }

    fun getCachedBlockOrNull(chunk: String): ParsedBlock? {
        return state.now.blocks.firstOrNull { it.chunk == chunk }
    }

    fun syncFromInput(blocks: List<ParsedBlock>) {
        state.set { copy(blocks = blocks) }
    }

    fun syncFromCollect(markdown: Markdown): List<ParsedBlock> {
        val blocks = parser.parseBlocks(markdown.value, true)
        state.set { copy(blocks = blocks) }
        return blocks
    }
}

data class MarkdownEditorState(
    val blocks: List<ParsedBlock> = emptyList()
)