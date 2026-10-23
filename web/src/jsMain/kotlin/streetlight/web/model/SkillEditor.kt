package streetlight.web.model

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.Messenger
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOrNull
import streetlight.model.data.ImageTarget
import streetlight.model.data.Skill
import streetlight.model.data.SkillEdit
import streetlight.model.data.SkillTag
import streetlight.model.data.interviewQuestions
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient

/** Edits [edit], a new skill or one of the star's, and saves it, its interview following its tags in [scope]. */
class SkillEditor(
    edit: SkillEdit,
    scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(SkillEditorState(
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
    val levelState = editState.mutableTapOf({ it.skillLevel }) { copy(skillLevel = it) }
    val yearState = state.mutableTapOf({ it.yearText }) {
        copy(yearText = it, edit = this.edit.copy(yearStarted = it.toIntOrNull() ?: this.edit.yearStarted))
    }

    val tagsState = editState.tapOf { it.tags }

    val imageEditor = ImageEditor(imageState, ImageTarget.Skill, api)
    val interviewEditor = InterviewEditor(
        interview = edit.interview,
        questionsTap = editState.tapOf { edit -> edit.tags.flatMap { it.interviewQuestions } },
        scope = scope,
    )

    /** Whether the edit carries [tag]. */
    fun toggleTag(tag: SkillTag) {
        val isTagged = tag !in stateNow.edit.tags
        editState.set { copy(tags = if (isTagged) tags + tag else tags - tag) }
    }

    /** Saves the skill, creating it when it is new, and returns it, or `null` when invalid or failed. */
    suspend fun submitSuspend(messenger: Messenger): Skill? {
        if (editState.now.name.isBlank()) {
            messenger.deliver("A skill needs a name.")
            return null
        }
        messenger.deliverSending()
        if (!imageEditor.finalizeImage(messenger)) return null
        val edit = editState.now.copy(interview = interviewEditor.interviewNow)
        return api.user.editSkill(edit).toDataOrNull(messenger)
    }
}

data class SkillEditorState(
    val edit: SkillEdit,
    val yearText: String,
)
