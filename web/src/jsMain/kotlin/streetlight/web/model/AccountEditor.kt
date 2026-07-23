package streetlight.web.model

import kampfire.model.AccountUpgradeRequest
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.handleOutcome
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.mutableFieldOf
import koala.model.storeOf
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Account
import streetlight.web.io.ApiClient

class AccountEditor(
    initialData: Account,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val session: StarSession,
) {
    private val state = storeOf(AccountEditorState(initialData))
    val stateNow get() = state.now
    val stateFlow = state.flow

    val editField = state.mutableFieldOf({ it.edit }) { copy(edit = it) }
    val nameField = editField.mutableFieldOf({ it.name ?: "" }) { copy(name = it) }

    private val emailField = editField.mutableFieldOf({ it.email }) { copy(email = it) }

    val messages = MessageStore()
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
            messages.set("Sending...", true)
            val isSuccess = api.updateAccount(editField.now).handleResponse(messages) ?: return@launch
            if (isSuccess) {
                messages.set("Saved.")
            }
        }
    }
}

data class AccountEditorState(
    val edit: Account
)