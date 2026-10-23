package streetlight.web.model

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.Messenger
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOrNull
import streetlight.model.data.ImageTarget
import streetlight.model.data.Talent
import streetlight.model.data.TalentEdit
import streetlight.model.data.TalentTag
import streetlight.model.data.interviewQuestions
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient

/** Edits [edit], a new talent or one of the star's, and saves it, its interview following its tags in [scope]. */
class TalentEditor(
    edit: TalentEdit,
    scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(TalentEditorState(
        edit = edit,
        yearText = edit.yearStarted.toString(),
    ))
    val stateNow get() = state.now

    val editState = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val nameState = editState.mutableTapOf({ it.name }) { copy(name = it) }
    val descriptionState = editState.mutableTapOf({ it.description ?: "".toMarkdown() }) {
        copy(description = it.takeIf(Markdown::isNotBlank))
    }
    val imageState = editState.mutableTapOf({ it.image }) { copy(image = it) }
    val levelState = editState.mutableTapOf({ it.talentLevel }) { copy(talentLevel = it) }
    val yearState = state.mutableTapOf({ it.yearText }) {
        copy(yearText = it, edit = this.edit.copy(yearStarted = it.toIntOrNull() ?: this.edit.yearStarted))
    }

    val tagsState = editState.tapOf { it.tags }

    val imageEditor = ImageEditor(imageState, ImageTarget.Talent, api)
    val interviewEditor = InterviewEditor(
        interview = edit.interview,
        questionsTap = editState.tapOf { edit -> edit.tags.flatMap { it.interviewQuestions } },
        scope = scope,
    )

    /** Whether the edit carries [tag]. */
    fun toggleTag(tag: TalentTag) {
        val isTagged = tag !in stateNow.edit.tags
        editState.set { copy(tags = if (isTagged) tags + tag else tags - tag) }
    }

    /** Saves the talent, creating it when it is new, and returns it, or `null` when invalid or failed. */
    suspend fun submitSuspend(messenger: Messenger): Talent? {
        if (editState.now.name.isBlank()) {
            messenger.deliver("A talent needs a name.")
            return null
        }
        messenger.deliverSending()
        if (!imageEditor.finalizeImage(messenger)) return null
        val edit = editState.now.copy(interview = interviewEditor.interviewNow)
        return api.user.editTalent(edit).toDataOrNull(messenger)
    }
}

data class TalentEditorState(
    val edit: TalentEdit,
    val yearText: String,
)
