package streetlight.web.model

import kampfire.api.Slug
import kampfire.api.toMarkdown
import kampfire.model.toDataOr
import koala.dom.MessageStore
import koala.model.dedup
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.DefaultLayout
import streetlight.model.data.Galaxy
import streetlight.model.data.MediaEdit
import streetlight.model.data.PageDesign
import streetlight.model.data.PostEdit
import streetlight.model.data.PostType
import streetlight.web.io.ApiClient

class MediaEditor(
    initialContent: MediaEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    val layoutEditor = LayoutEditor(initialContent.design?.layout ?: DefaultLayout.media, api)
    val themeEditor = ThemeEditor(initialContent.design?.theme)

    private val state = storeOf(ContentEditorState(initialContent))
    val stateFlow = state.flow
    val stateNow get() = state.now
    val editFlow = stateFlow.dedup { it.edit }
    val editNow get() = state.now.edit

    val editField = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val imageField = editField.mutableTapOf({ it.image }) { copy(image = it) }
    val titleField = editField.mutableTapOf({ it.title ?: "" }) { copy(title = it) }
    val subtitleField = editField.mutableTapOf({ it.subtitle ?: "" }) { copy(subtitle = it) }
    val textField = editField.mutableTapOf({ it.text ?: "".toMarkdown() }) { copy(text = it) }

    val message = MessageStore()
    val imageEditor = ImageEditor(imageField, api)

    init {
        scope.launch {
            editFlow.dedup { it.invalidMessage }.collect {
                message.deliver(it ?: "Looks good.")
            }
        }
    }

    // fun setTitle(title: String) = setContent { it.copy(title = title) }
    // fun setSubtitle(subtitle: String) = setContent { it.copy(subtitle = subtitle) }
    // fun setText(text: Markdown) = setContent { it.copy(text = text) }

    fun submitPost(galaxy: Galaxy? = null) {
        if (!editField.now.isValid) return
        val theme = themeEditor.buildTheme()
        scope.launch {
            imageEditor.finalizeImage(message)
            val layout = layoutEditor.buildLayout(toaster)
            val design = if (theme != null || layout != null) PageDesign(layout, theme) else null

            val edit = editField.now.copy(design = design)
            message.set("Posting...", true)

            val media = when (edit.mediaId) {
                null -> api.createMedia(edit)
                else -> api.updateMedia(edit)
            }.toDataOr(toaster) { return@launch }

            state.set { copy(slug = media.slug) }

            if (galaxy != null) {
                message.deliver("Posting to ${galaxy.name}...")
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