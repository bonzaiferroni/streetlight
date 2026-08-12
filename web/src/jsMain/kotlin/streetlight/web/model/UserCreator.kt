package streetlight.web.model

import kampfire.api.Username
import kampfire.api.obfuscatePassword
import kampfire.api.toUsername
import kampfire.api.toValidOutcome
import kampfire.model.AccountType
import kampfire.model.Ok
import kampfire.model.PrintLnMessenger
import kampfire.model.Problem
import kampfire.model.SignUpRequest
import kampfire.model.handleOutcome
import kampfire.model.handleResponse
import koala.dom.MessageStore
import koala.model.mutableTapOf
import koala.model.tapOf
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.web.io.ApiClient

class UserCreator(
    private val scope: CoroutineScope,
    private val gate: SessionGate,
    private val cred: CredentialStore,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(UserCreatorState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val emailEditor = EmailEditor(null, scope)
    val passwordEditor = PasswordEditor()
    val messages = MessageStore()

    val usernameField = state.mutableTapOf({ it.username }) { copy(username = it) }
    val isValidField = state.tapOf { it.isValid }
    val guestField = state.mutableTapOf({ it.guestUsername }) { copy(guestUsername = it) }

    val minAgeField = state.mutableTapOf({ it.isMinimumAge }) { copy(isMinimumAge = it) }

    init {
        scope.launch {
            api.checkGuest().handleResponse(PrintLnMessenger) { username ->
                state.set { copy(guestUsername = username) }
            }
        }
    }

    fun generateUsername() = scope.launch {
        val username = api.generateUsername().handleResponse(toaster) ?: return@launch
        state.set { copy(username = username.value)}
    }

    fun setUsername(username: String) = state.set { copy(username = username) }

    fun createAccount(accountType: AccountType) {
        val username = stateNow.username.trim().toUsername().toValidOutcome().handleOutcome(messages) ?: return
        val email = when (val emailOutcome = emailEditor.getOutcome()) {
            is Problem -> {
                emailOutcome.handleOutcome(messages)
                return
            }
            is Ok -> emailOutcome.data
        }
        val password = when (accountType) {
            AccountType.Guest -> null
            AccountType.Registered -> passwordEditor.getOutcome().handleOutcome(messages) ?: return
        }

        val request = SignUpRequest(
            username = username,
            password = password?.obfuscatePassword(),
            email = email,
            accountType = accountType,
            stayLoggedIn = true
        )
        messages.deliver("Creating account...")
        scope.launch {
            val isSuccess = api.createUser(request).handleResponse(messages) ?: return@launch
            if (isSuccess) {
                // cred.setFromSignup(request)
                cred.followUpAuth(true)
                gate.signIn(messages)
                if (accountType == AccountType.Guest) {
                    state.set { copy(guestUsername = username) }
                }
            }
        }
    }
}

data class UserCreatorState(
    val username: String = "",
    val isMinimumAge: Boolean = false,
    val guestUsername: Username? = null,
) {
    val isValid get() = isMinimumAge && username.toUsername().toValidOutcome().isOk

    companion object {
        const val MINIMUM_AGE = 17
    }
}