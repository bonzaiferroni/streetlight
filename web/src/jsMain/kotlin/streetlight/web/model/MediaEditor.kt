package streetlight.web.model

import streetlight.model.data.ImageTarget
import kampfire.api.Slug
import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.toDataOr
import koala.model.MessageStore
import koala.utility.dedup
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.DefaultLayout
import streetlight.model.data.Galaxy
import streetlight.model.data.MediaEdit
import streetlight.model.data.PostEdit
import streetlight.model.data.PostType
import streetlight.model.io.ApiClient

/** Creates or updates media. */
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

    val designer = DesignEditor(api, DefaultLayout.media, initialContent.design)

    val editField = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val imageField = editField.mutableTapOf({ it.image }) { copy(image = it) }
    val titleField = editField.mutableTapOf({ it.title ?: "" }) { copy(title = it) }
    val subtitleField = editField.mutableTapOf({ it.subtitle ?: "" }) { copy(subtitle = it) }
    val textField = editField.mutableTapOf({ it.text ?: "".toMarkdown() }) { copy(text = it.takeIf(Markdown::isNotBlank)) }

    val skillLinkEditor = SkillLinkEditor(
        skillIdsState = editField.mutableTapOf({ it.skillIds }) { copy(skillIds = it) },
        readLinked = initialContent.mediaId?.let { mediaId -> { api.media.readSkillIds(mediaId) } },
        scope = scope,
        api = api,
        toaster = toaster,
    )

    val message = MessageStore()
    val imageEditor = ImageEditor(imageField, ImageTarget.Media, api)

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

    /** Saves the media with its image and design, and posts it to [galaxy] when given. */
    fun submitPost(galaxy: Galaxy? = null) {
        if (!editField.now.isValid) return
        scope.launch {
            if (!imageEditor.finalizeImage(message)) return@launch
            val design = designer.build(message)

            val edit = editField.now.copy(design = design)
            message.set("Posting...", true)

            val media = when (edit.mediaId) {
                null -> api.media.createMedia(edit)
                else -> api.media.updateMedia(edit)
            }.toDataOr(toaster) { return@launch }

            state.set { copy(slug = media.slug) }

            if (galaxy != null) {
                message.deliver("Posting to ${galaxy.name}...")
                api.post.createPost(PostEdit(
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
    val slug: Slug? = null,
)