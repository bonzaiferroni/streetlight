package streetlight.web.model

import kampfire.model.AccountUpgradeRequest
import kampfire.model.Messenger
import kampfire.model.Ok
import kampfire.model.PrintLnMessenger
import kampfire.model.Problem
import kampfire.model.deliverSending
import kampfire.model.handleOutcome
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.fieldOf
import koala.model.mutableFieldOf
import koala.model.storeOf
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Account
import streetlight.model.data.EmailStatus
import streetlight.web.io.ApiClient

class AccountEditor(
    initialData: Account,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val session: SessionGate,
    private val toaster: Toaster,
) {
    private val state = storeOf(AccountEditorState(initialData))
    val stateNow get() = state.now
    val stateFlow = state.flow

    val editField = state.mutableFieldOf({ it.edit }) { copy(edit = it) }
    // val nameField = editField.mutableFieldOf({ it.name ?: "" }) { copy(name = it) }

    private val emailMutableField = editField.mutableFieldOf({ it.email }) { copy(email = it) }
    val emailAndStatusField = editField.fieldOf { Pair(it.email, it.emailStatus) }
    val isVerifySentField = state.fieldOf { it.emailVerificationSent }

    val emailEditor = EmailEditor(emailMutableField, scope)
    val passwordEditor = PasswordEditor()
    val emailMessages = MessageStore()

    init {
        val isUnverified = initialData.email != null && initialData.emailStatus == null
                || initialData.emailStatus == EmailStatus.Unverified
        if (isUnverified) {
            scope.launch("check verification status") {
                val isSent = api.readEmailVerificationIsSent().handleResponse(PrintLnMessenger) ?: return@launch
                state.set { copy(emailVerificationSent = isSent) }
                if (isSent) {
                    emailMessages.deliver("Check your inbox to verify your email.")
                }
            }
        }
    }

    fun completeRegistration(messenger: Messenger) {
        val email = when (val emailOutcome = emailEditor.getOutcome()) {
            is Problem -> {
                emailOutcome.handleOutcome(messenger)
                return
            }
            is Ok -> emailOutcome.data
        }

        val password = passwordEditor.getOutcome().handleOutcome(messenger) ?: return

        val request = AccountUpgradeRequest(password, email)
        scope.launch {
            val isSuccess = api.upgradeAccount(request).handleResponse(messenger) ?: return@launch
            if (isSuccess) {
                if (email != null) {
                    toaster.deliver("Check your inbox to verify your email.")
                }
                session.readUser(messenger)
            }
        }
    }

    fun verifyEmail() {
        scope.launch(::verifyEmail) {
            emailMessages.deliverSending()
            if (api.verifyEmail().handleResponse(emailMessages) == null) return@launch
            state.set { copy(emailVerificationSent = true) }
            emailMessages.deliver("Request sent, check your email.")
        }
    }

    fun changeEmail() {
        state.set { copy(edit = edit.copy(email = null, emailStatus = null), emailVerificationSent = false) }
    }

    fun removeEmail() {
        scope.launch(::removeEmail) {
            emailMessages.deliverSending()
            if (api.removeEmail().handleResponse(emailMessages) == null) return@launch
            state.set { copy(edit = edit.copy(email = null, emailStatus = null), emailVerificationSent = false) }
            emailMessages.deliver("Your email address has been removed from our database.")
        }
    }

    fun addEmail() {
        scope.launch(::addEmail) {
            val email = emailEditor.getOutcome().handleOutcome(emailMessages) ?: return@launch
            emailMessages.deliverSending()
            api.addEmail(email).handleResponse(emailMessages) ?: return@launch
            state.set { copy(edit = edit.copy(email = email, emailStatus = EmailStatus.Unverified), emailVerificationSent = true) }
            emailMessages.deliver("Check your inbox to verify your email.")
        }
    }
}

data class AccountEditorState(
    val edit: Account,
    val emailVerificationSent: Boolean = edit.emailStatus == EmailStatus.Verified,
)