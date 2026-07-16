package streetlight.web.model

import kampfire.api.Markdown
import kampfire.model.AccountUpgradeRequest
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.handleOutcome
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.tap
import koala.model.storeOf
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
    val editNow get() = state.now.edit
    val editFlow = stateFlow.tap { it.edit }
    val taglineFlow = editFlow.tap { it.tagline }
    val descriptionFlow = editFlow.tap { it.description }
    val nameFlow = editFlow.tap { it.name }

    val messages = MessageStore()
    val imageEditor = ImageEditor(initialData.image, api)
    val emailEditor = EmailEditor(initialData.email?.value ?: "")
    val passwordEditor = PasswordEditor()

    fun setTagline(tagline: String) = setEdit { it.copy(tagline = tagline) }
    fun setDescription(description: Markdown) = setEdit { it.copy(description = description) }
    fun setName(name: String?) = setEdit { it.copy(name = name) }

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

    private fun setEdit(block: (StarEdit) -> StarEdit) {
        state.set { it.copy(edit = block(it.edit)) }
    }
}

data class StarEditorState(
    val edit: StarEdit
)