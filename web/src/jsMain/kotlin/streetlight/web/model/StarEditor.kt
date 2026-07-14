package streetlight.web.model

import kampfire.api.Markdown
import koala.dom.MessageStore
import koala.model.tap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.StarEdit
import streetlight.web.io.ApiClient

class StarEditor(
    initialData: StarEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(StarEditorState(initialData))
    val stateNow get() = state.now
    val stateFlow = state.flow
    val editNow get() = state.now.edit
    val editFlow = stateFlow.tap { it.edit }
    val taglineFlow = editFlow.tap { it.tagline }
    val descriptionFlow = editFlow.tap { it.description }
    val nameFlow = editFlow.tap { it.name }

    val message = MessageStore()
    val imageEditor = ImageEditor(initialData.image, api)

    fun setTagline(tagline: String) = setEdit { it.copy(tagline = tagline) }
    fun setDescription(description: Markdown) = setEdit { it.copy(description = description) }
    fun setName(name: String?) = setEdit { it.copy(name = name) }

    private fun setEdit(block: (StarEdit) -> StarEdit) {
        state.set { it.copy(edit = block(it.edit)) }
    }
}

data class StarEditorState(
    val edit: StarEdit
)