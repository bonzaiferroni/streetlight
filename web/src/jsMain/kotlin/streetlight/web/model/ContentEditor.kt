package streetlight.web.model

import kampfire.model.Url
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.ContentEdit
import streetlight.web.ui.ViewModel

class ContentEditor(
    private val scope: CoroutineScope,
    override val app: Streetlight,
    initialContent: ContentEdit,
): ViewModel {

    private val state = storeOf(ContentEditorState(initialContent))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val contentFlow = stateFlow.mapDistinct { it.content }
    val contentNow get() = state.now.content

    fun setTitle(title: String) = setContent { it.copy(title = title) }

    fun setText(text: String) = setContent { it.copy(text = text) }

    fun setImage(url: Url?) = setContent { it.copy(imageRef = url) }

    private fun setContent(block: (ContentEdit) -> ContentEdit) {
        state.set { it.copy(content = block(stateNow.content)) }
    }
}

data class ContentEditorState(
    val content: ContentEdit,
)