package streetlight.web.model

import kampfire.api.Slug
import kampfire.model.Url
import kampfire.model.handleResponse
import koala.dom.UIMessage
import koala.dom.set
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.PostEdit
import streetlight.model.data.PostId
import streetlight.web.io.ApiClient

class PostEditor(
    initialContent: PostEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {

    private val state = storeOf(ContentEditorState(initialContent))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val msg = storeOf<UIMessage?>(null)

    val contentFlow = stateFlow.mapDistinct { it.content }
    val contentNow get() = state.now.content

    init {
        scope.launch {
            contentFlow.mapDistinct { it.invalidMessage }.collect {
                msg.set(it ?: "Looks good.")
            }
        }
    }

    fun setTitle(title: String) = setContent { it.copy(title = title) }

    fun setSubtitle(subtitle: String) = setContent { it.copy(subtitle = subtitle) }

    fun setText(text: String) = setContent { it.copy(text = text) }

    fun setImage(url: Url?) = setContent { it.copy(imageRef = url) }

    fun submitPost() {
        var content = contentNow.takeIf { it.isValid } ?: return
        scope.launch {
            val blobUrl = content.imageRef?.takeIf { it.isBlob }
            content = blobUrl?.let {
                val refUrl = api.uploadImage(blobUrl).handleResponse(msg::set)
                if (refUrl == null) {
                    msg.set("Unable to upload image.")
                    return@launch
                }

                content.copy(imageRef = refUrl)
            } ?: content

            when (content.postId) {
                null -> api.createPost(content)
                else -> api.editPost(content)
            }.handleResponse(toaster::toast) { slug ->
                state.set { it.copy(slug = slug) }
            }
        }
    }

    private fun setContent(block: (PostEdit) -> PostEdit) {
        state.set { it.copy(content = block(stateNow.content)) }
    }
}

data class ContentEditorState(
    val content: PostEdit,
    val slug: Slug? = null
)