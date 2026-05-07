package streetlight.web.model

import kampfire.model.Url
import koala.dom.UIMessage
import koala.dom.set
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.StarPostEdit
import streetlight.web.io.handleResponse
import streetlight.web.ui.ViewModel

class ContentEditor(
    private val scope: CoroutineScope,
    override val app: Streetlight,
    initialContent: StarPostEdit,
): ViewModel {

    private val state = storeOf(ContentEditorState(initialContent))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val message = storeOf(UIMessage())

    val contentFlow = stateFlow.mapDistinct { it.content }
    val contentNow get() = state.now.content

    init {
        scope.launch {
            contentFlow.mapDistinct { it.invalidMessage }.collect {
                message.set(it ?: "Looks good.")
            }
        }
    }

    fun setTitle(title: String) = setContent { it.copy(title = title) }

    fun setText(text: String) = setContent { it.copy(text = text) }

    fun setImage(url: Url?) = setContent { it.copy(imageRef = url) }

    fun submitPost() {
        var content = contentNow.takeIf { it.isValid } ?: return
        scope.launch {
            val blobUrl = content.imageRef?.takeIf { it.isBlob }
            content = blobUrl?.let {
                val refUrl = api.uploadImage(blobUrl)
                if (refUrl == null) {
                    message.set("Unable to upload image.")
                    return@launch
                }

                content.copy(imageRef = refUrl)
            } ?: content

            when (content.postId) {
                null -> api.createPost(content)
                else -> api.editPost(content)
            }.handleResponse(toaster::toast) {
                app.stagePostAndGo(it)
            }
        }
    }

    private fun setContent(block: (StarPostEdit) -> StarPostEdit) {
        state.set { it.copy(content = block(stateNow.content)) }
    }
}

data class ContentEditorState(
    val content: StarPostEdit,
)