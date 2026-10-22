package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.toDataOrNull
import streetlight.model.data.Talent
import streetlight.model.data.TalentEdit
import streetlight.model.data.TalentTag
import streetlight.web.io.ApiClient

/** Edits [edit], a new talent or one of the star's, and saves it. */
class TalentEditor(
    edit: TalentEdit,
    private val api: ApiClient,
) {
    private val state = storeOf(TalentEditorState(
        edit = edit,
        yearText = edit.yearStarted.toString(),
    ))
    val stateNow get() = state.now

    val editState = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val nameField = editState.mutableTapOf({ it.name }) { copy(name = it) }
    val descriptionField = editState.mutableTapOf({ it.description ?: "" }) {
        copy(description = it.takeIf(String::isNotBlank))
    }
    val levelField = editState.mutableTapOf({ it.talentLevel }) { copy(talentLevel = it) }
    val yearField = state.mutableTapOf({ it.yearText }) {
        copy(yearText = it, edit = this.edit.copy(yearStarted = it.toIntOrNull() ?: this.edit.yearStarted))
    }

    /** Whether the edit carries [tag]. */
    fun tagField(tag: TalentTag) = editState.mutableTapOf({ tag in it.tags }) { isTagged ->
        copy(tags = if (isTagged) tags + tag else tags - tag)
    }

    /** Saves the talent, creating it when it is new, and returns it, or `null` when invalid or failed. */
    suspend fun submitSuspend(messenger: Messenger): Talent? {
        if (editState.now.name.isBlank()) {
            messenger.deliver("A talent needs a name.")
            return null
        }
        messenger.deliverSending()
        return api.user.editTalent(editState.now).toDataOrNull(messenger)
    }
}

data class TalentEditorState(
    val edit: TalentEdit,
    val yearText: String,
)
