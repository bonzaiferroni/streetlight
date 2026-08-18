package koala.dom

import kampfire.api.Markdown
import koala.model.mutableTapOf
import koala.model.storeOf

class MarkdownEditor() {
    private val state = storeOf(MarkdownEditorState())

    val linesState = state.mutableTapOf({ it.lines }) { copy(lines = it)}
}

data class MarkdownEditorState(
    val lines: List<String> = emptyList()
)