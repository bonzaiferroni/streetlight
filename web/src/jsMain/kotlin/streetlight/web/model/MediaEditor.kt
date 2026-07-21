package streetlight.web.model

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.toMarkdown
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.dedup
import koala.model.mutableFieldOf
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.MediaEdit
import streetlight.model.data.PostEdit
import streetlight.model.data.PostType
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
    val editFlow = stateFlow.dedup { it.edit }
    val editNow get() = state.now.edit

    val message = MessageStore()
    val imageEditor = ImageEditor(initialContent.image, api)

    val editField = state.mutableFieldOf({ it.edit }) { copy(edit = it) }
    val titleField = editField.mutableFieldOf({ it.title ?: "" }) { copy(title = it) }
    val subtitleField = editField.mutableFieldOf({ it.subtitle ?: "" }) { copy(subtitle = it) }
    val textField = editField.mutableFieldOf({ it.text ?: "".toMarkdown() }) { copy(text = it) }

    init {
        scope.launch {
            editFlow.dedup { it.invalidMessage }.collect {
                message.receive(it ?: "Looks good.")
            }
        }
    }

    // fun setTitle(title: String) = setContent { it.copy(title = title) }
    // fun setSubtitle(subtitle: String) = setContent { it.copy(subtitle = subtitle) }
    // fun setText(text: Markdown) = setContent { it.copy(text = text) }

    fun submitPost(galaxy: Galaxy? = null) {
        scope.launch {
            val edit = editNow.takeIf { it.isValid } ?: return@launch
            val image = imageEditor.finalizeImage(message)
            message.set("Posting...", true)

            val media = when (edit.mediaId) {
                null -> api.createMedia(edit.copy(image = image))
                else -> api.updateMedia(edit.copy(image = image))
            }.handleResponse(toaster) { media ->
                state.setValue { it.copy(slug = media.slug) }
                media
            }

            if (media != null && galaxy != null) {
                message.receive("Posting to ${galaxy.name}...")
                api.createPost(PostEdit(
                    postId = null,
                    galaxyId = galaxy.galaxyId,
                    postType = PostType.Media,
                    recordId = media.mediaId.value,
                ))
            }
        }
    }
}

data class ContentEditorState(
    val edit: MediaEdit,
    val slug: Slug? = null
)