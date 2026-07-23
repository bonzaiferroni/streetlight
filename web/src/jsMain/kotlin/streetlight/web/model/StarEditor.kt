package streetlight.web.model

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.AccountUpgradeRequest
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.handleOutcome
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.dedup
import koala.model.mutableFieldOf
import koala.model.storeOf
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.StarEdit
import streetlight.web.io.ApiClient

class StarEditor(
    initialData: StarEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val session: StarSession,
) {
    private val state = storeOf(StarEditorState(initialData))
    val stateNow get() = state.now
    val stateFlow = state.flow

    val editField = state.mutableFieldOf({ it.edit }) { copy(edit = it) }
    val imageField = editField.mutableFieldOf({ it.image }) { copy(image = it) }
    val nameField = editField.mutableFieldOf({ it.name ?: "" }) { copy(name = it) }
    val descriptionField = editField.mutableFieldOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val taglineField = editField.mutableFieldOf({ it.tagline ?: "" }) { copy(tagline = it) }

    private val emailField = editField.mutableFieldOf({ it.email }) { copy(email = it) }

    val messages = MessageStore()
    val imageEditor = ImageEditor(imageField, api)
    val emailEditor = EmailEditor(emailField, scope)
    val passwordEditor = PasswordEditor()

    fun completeRegistration() {
        val email = when (val emailOutcome = emailEditor.getOutcome()) {
            is Problem -> {
                emailOutcome.handleOutcome(messages)
                return
            }
            is Ok -> emailOutcome.data
        }

        val password = passwordEditor.getOutcome().handleOutcome(messages) ?: return

        val request = AccountUpgradeRequest(password, email)
        scope.launch {
            val isSuccess = api.upgradeAccount(request).handleResponse(messages) ?: return@launch
            if (isSuccess) {
                session.readUser(messages)
            }
        }
    }

    fun submit() {
        scope.launch(::submit) {
            imageEditor.finalizeImage(messages)

            messages.set("Sending...", true)

            val star = api.updateStar(editField.now).handleResponse(messages) ?: return@launch
            messages.set("Saved.")
            session.setUser(star)
        }
    }

    // private fun setEdit(block: (StarEdit) -> StarEdit) {
    //     state.setValue { it.copy(edit = block(it.edit)) }
    // }
}

data class StarEditorState(
    val edit: StarEdit
)