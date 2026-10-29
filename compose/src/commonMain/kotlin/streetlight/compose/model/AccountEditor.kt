package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kampfire.api.EmailAddress
import kampfire.api.Password
import kampfire.api.obfuscatePassword
import kampfire.api.toEmailAddress
import kampfire.api.toValidOutcome
import kampfire.model.AccountUpgradeRequest
import kampfire.model.EmailChange
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.PasswordChange
import kampfire.model.PasswordVerification
import kampfire.model.Problem
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.model.MessageStore
import kotlinx.coroutines.launch
import streetlight.model.data.Account
import streetlight.model.data.EmailStatus
import streetlight.model.data.viableEmail
import streetlight.model.io.ApiClient

/**
 * The signed-in star's [account] and its settings: completing a guest's registration, and adding, verifying, removing
 * or changing its email and password. A change to an account with a viable email asks for the current password. The
 * cousin of the web's `AccountEditor`, with its `EmailEditor` and `PasswordEditor` folded in.
 */
class AccountEditor(account: Account, private val api: ApiClient, private val session: StarSession) : ViewModel() {
    private val store = storeOf(AccountEditorState(account))

    val accountState = store.tapOf { it.account }
    val emailField = store.mutableTapOf({ it.email }) { copy(email = it) }
    val passwordField = store.mutableTapOf({ it.password }) { copy(password = it) }
    val confirmationField = store.mutableTapOf({ it.confirmation }) { copy(confirmation = it) }
    val verifyPasswordField = store.mutableTapOf({ it.verifyPassword }) { copy(verifyPassword = it) }
    val isEditingPasswordField = store.mutableTapOf({ it.isEditingPassword }) { copy(isEditingPassword = it) }
    val isRemovingEmailField = store.mutableTapOf({ it.isRemovingEmail }) { copy(isRemovingEmail = it) }
    val isChangingEmailState = store.tapOf { it.isChangingEmail }
    val isVerifySentState = store.tapOf { it.isVerificationSent }
    val hasVerifiedEmailState = store.tapOf { it.hasVerifiedEmail }
    val emailMessage = MessageStore()
    val passwordMessage = MessageStore()
    val registerMessage = MessageStore()

    init {
        val isUnverified = account.email != null && account.emailStatus.let { it == null || it == EmailStatus.Unverified }
        if (isUnverified) {
            viewModelScope.launch {
                val isSent = (api.accountAction.readEmailVerificationIsSent() as? Ok)?.data ?: return@launch
                store.set { copy(isVerificationSent = isSent) }
                if (isSent) emailMessage.deliver(VerifyInboxMessage)
            }
        }
    }

    /** Upgrades a guest's account with the password and optional email in the form, then reads the star again. */
    fun completeRegistration() {
        val state = store.now
        val email = emailOf(state.email).toDataOr(registerMessage) { return }
        val password = passwordOf(state).toDataOr(registerMessage) { return }
        val request = AccountUpgradeRequest(password.obfuscatePassword(), email)

        registerMessage.deliverSending("Registering...")
        viewModelScope.launch {
            val isUpgraded = api.user.upgradeAccount(request).toDataOr(registerMessage) { return@launch }
            if (!isUpgraded) return@launch
            store.set {
                copy(
                    account = account.copy(email = email, emailStatus = email?.let { EmailStatus.Unverified }),
                    password = "",
                    confirmation = "",
                    isVerificationSent = email != null,
                )
            }
            registerMessage.clear()
            emailMessage.set(email?.let { VerifyInboxMessage })
            session.restore()
        }
    }

    /** Asks the server to send the verification email for the account's address again. */
    fun verifyExistingEmail() {
        emailMessage.deliverSending()
        viewModelScope.launch {
            api.accountAction.verifyExistingEmail().toDataOr(emailMessage) { return@launch }
            store.set { copy(isVerificationSent = true) }
            emailMessage.deliverSuccess("Request sent, check your email.")
        }
    }

    /** Clears the email so a new one can be entered; nothing is sent until [addEmail]. */
    fun changeEmail() {
        store.set {
            copy(
                account = account.copy(email = null, emailStatus = null),
                isVerificationSent = false,
                isChangingEmail = true,
                verifyPassword = "",
            )
        }
        emailMessage.clear()
    }

    /** Removes the account's email, confirmed by the current password when the email is verified. */
    fun removeEmail() {
        val state = store.now
        val password = when (state.hasVerifiedEmail) {
            true -> state.verifyPassword.takeIf { it.isNotBlank() }?.let { Password(it).obfuscatePassword() } ?: return
            false -> ""
        }
        emailMessage.deliverSending()
        viewModelScope.launch {
            api.accountAction.removeEmail(PasswordVerification(password)).toDataOr(emailMessage) { return@launch }
            store.set {
                copy(
                    account = account.copy(email = null, emailStatus = null),
                    isVerificationSent = false,
                    isRemovingEmail = false,
                    hasVerifiedEmail = false,
                    verifyPassword = "",
                )
            }
            emailMessage.deliverSuccess("Your email address has been removed from our database.")
        }
    }

    /** Sets the typed email as the account's, unverified, and sends its verification. */
    fun addEmail() {
        val state = store.now
        val email = emailOf(state.email).toDataOr(emailMessage) { return } ?: return
        val passwordNow = when (state.account.viableEmail) {
            null -> null
            else -> state.verifyPassword.takeIf { it.isNotBlank() }?.let { Password(it) } ?: return
        }
        emailMessage.deliverSending()
        viewModelScope.launch {
            api.accountAction.addEmail(EmailChange(passwordNow?.obfuscatePassword(), email))
                .toDataOr(emailMessage) { return@launch }
            store.set {
                copy(
                    account = account.copy(email = email, emailStatus = EmailStatus.Unverified),
                    email = "",
                    isVerificationSent = true,
                    isChangingEmail = false,
                    hasVerifiedEmail = false,
                    verifyPassword = "",
                )
            }
            emailMessage.deliver(VerifyInboxMessage)
        }
    }

    /** Changes the password to the typed one, confirmed by the current password when the email is viable. */
    fun changePassword() {
        val state = store.now
        val password = passwordOf(state).toDataOr(passwordMessage) { return }
        val passwordNow = when (state.account.viableEmail) {
            null -> null
            else -> state.verifyPassword.takeIf { it.isNotBlank() }?.let { Password(it) } ?: return
        }
        passwordMessage.deliverSending()
        viewModelScope.launch {
            val change = PasswordChange(passwordNow?.obfuscatePassword(), password.obfuscatePassword())
            api.accountAction.changePassword(change).toDataOr(passwordMessage) { return@launch }
            store.set {
                copy(
                    password = "",
                    confirmation = "",
                    verifyPassword = "",
                    isEditingPassword = false,
                )
            }
            passwordMessage.deliverSuccess("Password successfully changed.")
        }
    }

    /** Sends a link to reset the password to the account's viable email. */
    fun resetPassword() {
        val email = store.now.account.viableEmail ?: return
        passwordMessage.deliverSending()
        viewModelScope.launch {
            api.accountAction.resetPassword(email).toDataOr(passwordMessage) { return@launch }
            passwordMessage.deliverSuccess("Check your email inbox for a link to reset your password.")
        }
    }
}

/** The typed email, validated, or `null` when blank, as it is optional. */
private fun emailOf(text: String): Outcome<EmailAddress?> = when {
    text.isBlank() -> Ok(null)
    else -> text.trim().toEmailAddress().toValidOutcome()
}

/** The typed password, validated, or a problem when its confirmation differs. */
private fun passwordOf(state: AccountEditorState): Outcome<Password> = when {
    state.password != state.confirmation -> Problem("Password input does not match")
    else -> Password(state.password).toValidOutcome()
}

/** The [account] as edited, and the fields of its forms. */
data class AccountEditorState(
    val account: Account,
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val verifyPassword: String = "",
    val isVerificationSent: Boolean = account.emailStatus == EmailStatus.Verified,
    val isEditingPassword: Boolean = false,
    val isChangingEmail: Boolean = false,
    val isRemovingEmail: Boolean = false,
    val hasVerifiedEmail: Boolean = account.email != null && account.emailStatus == EmailStatus.Verified,
)

private const val VerifyInboxMessage = "Check your inbox to verify your email."
