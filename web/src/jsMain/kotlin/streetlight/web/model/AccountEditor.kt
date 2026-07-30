package streetlight.web.model

import kampfire.api.Email
import kampfire.api.Password
import kampfire.api.obfuscatePassword
import kampfire.model.AccountUpgradeRequest
import kampfire.model.EmailChange
import kampfire.model.Messenger
import kampfire.model.Ok
import kampfire.model.PasswordChange
import kampfire.model.PrintLnMessenger
import kampfire.model.Problem
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
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
import streetlight.model.data.viableEmail
import streetlight.web.io.ApiClient

class AccountEditor(
    private val initialAccount: Account,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val session: SessionGate,
    private val toaster: Toaster,
) {
    private val state = storeOf(AccountEditorState(initialAccount))
    val stateNow get() = state.now
    val stateFlow = state.flow

    val editField = state.mutableFieldOf({ it.account }) { copy(account = it) }
    // val nameField = editField.mutableFieldOf({ it.name ?: "" }) { copy(name = it) }

    private val emailMutableField = editField.mutableFieldOf({ it.email }) { copy(email = it) }
    val emailAndStatusField = editField.fieldOf { Pair(it.email, it.emailStatus) }
    val isVerifySentField = state.fieldOf { it.emailVerificationSent }
    val isEditingPasswordField = state.mutableFieldOf({ it.isEditingPassword }) { copy(isEditingPassword = it) }
    val passwordNowField = state.mutableFieldOf({ it.passwordNow }) { copy(passwordNow = it) }

    val emailEditor = EmailEditor(emailMutableField, scope)
    val passwordEditor = PasswordEditor()
    val emailMessages = MessageStore()
    val hasVerifiedEmail = initialAccount.email != null && initialAccount.emailStatus == EmailStatus.Verified

    init {
        val isUnverified = initialAccount.email != null && initialAccount.emailStatus == null
                || initialAccount.emailStatus == EmailStatus.Unverified
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

        val request = AccountUpgradeRequest(password.obfuscatePassword(), email)
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

    fun verifyExistingEmail() {
        scope.launch(::verifyExistingEmail) {
            emailMessages.deliverSending()
            if (api.verifyExistingEmail().handleResponse(emailMessages) == null) return@launch
            state.set { copy(emailVerificationSent = true) }
            emailMessages.deliver("Request sent, check your email.")
        }
    }

    fun changeEmail() {
        state.set { copy(
            account = account.copy(email = null, emailStatus = null),
            emailVerificationSent = false,
            isChangingEmail = true,
            passwordNow = "",
        ) }
    }

    fun removeEmail() {
        scope.launch(::removeEmail) {
            emailMessages.deliverSending()
            if (api.removeEmail().handleResponse(emailMessages) == null) return@launch
            state.set { copy(
                account = account.copy(email = null, emailStatus = null),
                emailVerificationSent = false,
            ) }
            emailMessages.deliver("Your email address has been removed from our database.")
        }
    }

    fun addEmail() {
        val email = emailEditor.getOutcome().handleOutcome(emailMessages) ?: return
        val passwordNow = if (stateNow.account.viableEmail != null) {
            stateNow.passwordNow.takeIf { it.isNotBlank() }?.let { Password(it) } ?: return
        } else null
        scope.launch(::addEmail) {
            emailMessages.deliverSending()
            api.addEmail(EmailChange(
                passwordNow = passwordNow?.obfuscatePassword(),
                newEmail = email,
            )).handleResponse(emailMessages) ?: return@launch
            state.set { copy(
                account = account.copy(email = email, emailStatus = EmailStatus.Unverified),
                emailVerificationSent = true,
                passwordNow = "",
            ) }
            emailMessages.deliver("Check your inbox to verify your email.")
        }
    }

    fun changePassword(messenger: Messenger) {
        val password = passwordEditor.getOutcome().handleOutcome(messenger) ?: return
        val passwordNow = if (stateNow.account.viableEmail != null) {
            stateNow.passwordNow.takeIf { it.isNotBlank() }?.let { Password(it) } ?: return
        } else null
        scope.launch(::changePassword) {
            messenger.deliverSending()
            if (api.changePassword(PasswordChange(
                passwordNow = passwordNow?.obfuscatePassword(),
                newPassword = password.obfuscatePassword()
            )).handleOutcome(messenger) == null) return@launch
            passwordEditor.clear()
            state.set { copy(isEditingPassword = false, passwordNow = "")}
            messenger.deliver(UIMessage("Password successfully changed.", UIMessageType.Success))
        }
    }

    fun resetPassword(email: Email, messenger: Messenger) {
        scope.launch(::resetPassword) {
            messenger.deliverSending()
            api.resetPassword(email).handleResponse(messenger) ?: return@launch
            messenger.deliver("Check your email inbox for a link to reset your password.")
        }
    }
}

data class AccountEditorState(
    val account: Account,
    val emailVerificationSent: Boolean = account.emailStatus == EmailStatus.Verified,
    val isEditingPassword: Boolean = false,
    val isChangingEmail: Boolean = false,
    val passwordNow: String = "",
) {
    // val validEmail = edit.email.takeIf { edit.emailStatus }
}