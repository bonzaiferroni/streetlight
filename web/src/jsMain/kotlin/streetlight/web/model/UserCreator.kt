package streetlight.web.model

import kampfire.api.Username
import kampfire.api.toUsername
import kampfire.model.AccountType
import kampfire.model.SignUpRequest
import kampfire.model.getDataOrNull
import kampfire.model.handleOutcome
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.web.io.ApiClient
import kotlin.uuid.Uuid

class UserCreator(
    private val scope: CoroutineScope,
    private val gate: StarSession,
    private val cred: CredentialStore,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(UserCreatorState())
    private val requestNow get() = state.now.request

    val usernameFlow = state.flow.mapDistinct { it.request.username.value }
    val emailFlow = state.flow.mapDistinct { it.request.email ?: "" }
    val passwordFlow = state.flow.mapDistinct { it.request.password }
    val confirmPasswordFlow = state.flow.mapDistinct { it.confirmPassword }
    val isValidFlow = state.flow.mapDistinct { it.isValid }

    fun generateUsername() = scope.launch {
        val username = api.generateUsername().getDataOrNull()
        setRequest { it.copy(username = username ?: Username.Empty)}
    }

    fun setUsername(username: String) = setRequest { it.copy(username = username.toUsername()) }
    fun setEmail(email: String) = setRequest { it.copy(email = email) }
    fun setPassword(password: String) = setRequest { it.copy(password = password) }
    fun setConfirmPassword(confirmPassword: String) = state.set { it.copy(confirmPassword = confirmPassword) }
    fun setIsMinimumAge(value: Boolean) = state.set { it.copy(isMinimumAge = value) }

    fun createAccount(accountType: AccountType) {
        val request = when (accountType) {
            AccountType.Guest -> requestNow.copy(
                accountType = AccountType.Guest,
                password = Uuid.random().toString(),
            )
            AccountType.Registered -> requestNow.copy(
                accountType = AccountType.Registered,
            )
        }.also{ println(it.isValid) }.takeIf { it.isValid } ?: return
        scope.launch {
            val isSuccess = api.createUser(request).handleOutcome(toaster::toast) ?: return@launch
            if (isSuccess) {
                cred.setFromSignup(request)
                gate.signIn()
            }
        }
    }

    private fun setRequest(block: (SignUpRequest) -> SignUpRequest) {
        state.set { it.copy(request = block(requestNow)) }
    }
}

data class UserCreatorState(
    val request: SignUpRequest = SignUpRequest(),
    val confirmPassword: String = "",
    val isMinimumAge: Boolean = false,
) {
    val isValid get() = isMinimumAge && request.isValid && confirmPassword == request.password

    companion object {
        const val MINIMUM_AGE = 17
    }
}