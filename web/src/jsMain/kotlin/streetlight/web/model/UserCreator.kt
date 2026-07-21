package streetlight.web.model

import kampfire.api.Username
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
import koala.model.mutableFieldOf
import koala.model.fieldOf
import koala.model.protoFieldOf
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.web.io.ApiClient

class UserCreator(
    private val scope: CoroutineScope,
    private val gate: StarSession,
    private val cred: CredentialStore,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(UserCreatorState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val emailEditor = EmailEditor("")
    val passwordEditor = PasswordEditor()
    val messages = MessageStore()

    val usernameField = state.mutableFieldOf({ it.username }) { copy(username = it) }
    val isValidField = state.fieldOf { it.isValid }
    val guestField = state.mutableFieldOf({ it.guestUsername }) { copy(guestUsername = it) }

    val minAgeField = state.mutableFieldOf({ it.isMinimumAge }) { copy(isMinimumAge = it) }

    init {
        scope.launch {
            api.checkGuest().handleResponse(PrintLnMessenger) { username ->
                state.setValue { it.copy(guestUsername = username) }
            }
        }
    }

    fun generateUsername() = scope.launch {
        val username = api.generateUsername().handleResponse(toaster) ?: return@launch
        state.setValue { it.copy(username = username.value)}
    }

    fun setUsername(username: String) = state.setValue { it.copy(username = username) }

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
            password = password,
            email = email,
            accountType = accountType,
            stayLoggedIn = true
        )
        messages.receive("Creating account...")
        scope.launch {
            val isSuccess = api.createUser(request).handleResponse(messages) ?: return@launch
            if (isSuccess) {
                // cred.setFromSignup(request)
                cred.followUpAuth(true)
                gate.signIn(messages)
                if (accountType == AccountType.Guest) {
                    state.setValue { it.copy(guestUsername = username) }
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