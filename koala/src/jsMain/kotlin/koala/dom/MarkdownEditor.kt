package koala.dom

import kampfire.api.Markdown
import koala.markdown.MarkdownParser
import koala.markdown.ParsedBlock
import kampfire.model.mutableTapOf
import kampfire.model.storeOf

/** The parsed blocks of a markdown editor, cached so an unchanged chunk is not parsed again. */
class MarkdownEditor() {
    private val state = storeOf(MarkdownEditorState())
    private val parser = MarkdownParser()

    val blocksState = state.mutableTapOf({ it.blocks }) { copy(blocks = it) }

    /** The cached block whose text is [chunk], or `null`. */
    fun getCachedBlockOrNull(chunk: String): ParsedBlock? {
        return state.now.blocks.firstOrNull { it.chunk == chunk }
    }

    /** Replaces the cached blocks with those parsed from the editor's input. */
    fun syncFromInput(blocks: List<ParsedBlock>) {
        state.set { copy(blocks = blocks) }
    }

    /** Parses [markdown] from a change outside the editor, caching and returning its blocks. */
    fun syncFromCollect(markdown: Markdown): List<ParsedBlock> {
        val blocks = parser.parseBlocks(markdown.value, true)
        state.set { copy(blocks = blocks) }
        return blocks
    }
}

data class MarkdownEditorState(
    val blocks: List<ParsedBlock> = emptyList()
)