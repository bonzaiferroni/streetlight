package streetlight.web.model

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.model.Url
import kampfire.model.handleOutcome
import koala.dom.MessageStore
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.MediaEdit
import streetlight.web.io.ApiClient

class MediaEditor(
    initialContent: MediaEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {

    private val state = storeOf(ContentEditorState(initialContent))
    val stateFlow = state.flow
    val stateNow get() = state.now

    val message = MessageStore()

    val editFlow = stateFlow.mapDistinct { it.edit }
    val editNow get() = state.now.edit

    init {
        scope.launch {
            editFlow.mapDistinct { it.invalidMessage }.collect {
                message.set(it ?: "Looks good.")
            }
        }
    }

    fun setTitle(title: String) = setContent { it.copy(title = title) }

    fun setSubtitle(subtitle: String) = setContent { it.copy(subtitle = subtitle) }

    fun setText(text: Markdown) = setContent { it.copy(text = text) }

    fun setImageUrl(url: Url?) = setContent { it.copy(imageRef = url) }

    fun submitPost() {
        scope.launch {
            if (!uploadImageIfBlob()) return@launch
            val edit = editNow.takeIf { it.isValid } ?: return@launch
            message.set("Posting...", true)

            when (edit.mediaId) {
                null -> api.createMedia(edit)
                else -> api.updateMedia(edit)
            }.handleOutcome(toaster::toast) { slug ->
                state.set { it.copy(slug = slug) }
            }
        }
    }

    private fun setContent(block: (MediaEdit) -> MediaEdit) {
        state.set { it.copy(edit = block(stateNow.edit)) }
    }

    private suspend fun uploadImageIfBlob(): Boolean {
        val blobUrl = editNow.imageRef?.takeIf { it.isBlob } ?: return true
        message.set("Uploading image...", true)
        val refUrl = api.uploadImage(blobUrl).handleOutcome(message::set)
        if (refUrl == null) {
            message.set("Unable to upload image.")
            return false
        }
        setImageUrl(refUrl)
        return true
    }
}

data class ContentEditorState(
    val edit: MediaEdit,
    val slug: Slug? = null
)