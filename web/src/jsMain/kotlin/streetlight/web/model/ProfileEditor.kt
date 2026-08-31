package streetlight.web.model

import kampfire.api.toMarkdown
import kampfire.model.toDataOr
import koala.dom.MessageStore
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.StarEdit
import streetlight.web.io.ApiClient

class ProfileEditor(
    initialData: StarEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val session: SessionGate,
) {
    private val state = storeOf(ProfileEditorState(initialData))
    val stateNow get() = state.now
    val stateFlow = state.flow

    val editField = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val imageField = editField.mutableTapOf({ it.image }) { copy(image = it) }
    val descriptionField = editField.mutableTapOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val taglineField = editField.mutableTapOf({ it.tagline ?: "" }) { copy(tagline = it) }

    val messages = MessageStore()
    val imageEditor = ImageEditor(imageField, api)

    fun submit() {
        scope.launch(::submit) {
            imageEditor.finalizeImage(messages)

            messages.set("Sending...", true)

            val star = api.updateProfile(editField.now).toDataOr(messages) { return@launch }
            messages.set("Saved.")
            session.setUser(star)
        }
    }
}

data class ProfileEditorState(
    val edit: StarEdit
)