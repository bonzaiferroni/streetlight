package streetlight.compose.model

import kampfire.api.EmailAddress
import kampfire.api.Password
import kampfire.api.obfuscatePassword
import kampfire.api.toEmailAddress
import kampfire.api.toUsername
import kampfire.api.toValidOutcome
import kampfire.model.AccountType
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.SignUpRequest
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.model.MessageStore
import koala.model.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.io.ApiClient

/**
 * The form to create a guest or registered account, which signs the new star in through [session]. A plain class, not
 * a view model, built by the dialog that shows it in its [scope]. The cousin of the web's `UserCreator`.
 */
class UserCreator(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val session: StarSession,
    private val toaster: Toaster,
) {
    private val store = storeOf(UserCreatorState())

    val usernameField = store.mutableTapOf({ it.username }) { copy(username = it) }
    val emailField = store.mutableTapOf({ it.email }) { copy(email = it) }
    val passwordField = store.mutableTapOf({ it.password }) { copy(password = it) }
    val confirmationField = store.mutableTapOf({ it.confirmation }) { copy(confirmation = it) }
    val minAgeField = store.mutableTapOf({ it.isMinimumAge }) { copy(isMinimumAge = it) }
    val isValidState = store.tapOf { it.isValid }
    val messages = MessageStore()

    /** Fills the username with one the server makes up. */
    fun generateUsername() {
        scope.launch {
            val username = api.user.generateUsername().toDataOr(toaster) { return@launch }
            store.set { copy(username = username.value) }
        }
    }

    /** Creates an account of [accountType] from the form, then signs it in. */
    fun createAccount(accountType: AccountType) {
        val state = store.now
        if (state.isCreating) return
        val username = state.username.trim().toUsername().toValidOutcome().toDataOr(messages) { return }
        val email = emailOf(state.email).toDataOr(messages) { return }
        val password = when (accountType) {
            AccountType.Guest -> null
            AccountType.Registered -> passwordOf(state).toDataOr(messages) { return }
        }
        val request = SignUpRequest(
            username = username,
            password = password?.obfuscatePassword(),
            email = email,
            accountType = accountType,
            stayLoggedIn = true,
        )

        store.set { copy(isCreating = true) }
        messages.deliverSending("Creating account...")
        scope.launch {
            api.user.createUser(request).toDataOr(messages) { return@launch store.set { copy(isCreating = false) } }
            session.restore()
            store.set { copy(isCreating = false) }
        }
    }
}

/** The typed email, validated, or `null` when blank, as it is optional. */
private fun emailOf(text: String): Outcome<EmailAddress?> = when {
    text.isBlank() -> Ok(null)
    else -> text.trim().toEmailAddress().toValidOutcome()
}

/** The typed password, validated, or a problem when its confirmation differs. */
private fun passwordOf(state: UserCreatorState): Outcome<Password> = when {
    state.password != state.confirmation -> Problem("Password input does not match")
    else -> Password(state.password).toValidOutcome()
}

/** The fields of a [UserCreator], and whether an account [isCreating]. */
data class UserCreatorState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val isMinimumAge: Boolean = false,
    val isCreating: Boolean = false,
) {
    val isValid get() = isMinimumAge && username.trim().toUsername().toValidOutcome() is Ok

    companion object {
        const val MINIMUM_AGE = 17
    }
}
