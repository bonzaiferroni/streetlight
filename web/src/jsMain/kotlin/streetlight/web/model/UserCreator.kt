package streetlight.web.model

import kampfire.model.SignUpRequest
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.utils.isValid
import streetlight.web.io.ApiClient

class UserCreator(
    private val scope: CoroutineScope,
    private val gate: UserGate,
    private val cred: CredentialStore,
    private val api: ApiClient,
) {
    private val state = storeOf(UserCreatorState())
    private val requestNow get() = state.now.request

    val usernameFlow = state.flow.mapDistinct { it.request.username }
    val emailFlow = state.flow.mapDistinct { it.request.email ?: "" }
    val passwordFlow = state.flow.mapDistinct { it.request.password }
    val confirmPasswordFlow = state.flow.mapDistinct { it.confirmPassword }
    val isValidFlow = state.flow.mapDistinct { it.isValid }

    fun setUsername(username: String) {
        setRequest { it.copy(username = username) }
    }

    fun setEmail(email: String) {
        setRequest { it.copy(email = email) }
    }

    fun setPassword(password: String) {
        setRequest { it.copy(password = password) }
    }

    fun setConfirmPassword(confirmPassword: String) {
        state.set { it.copy(confirmPassword = confirmPassword) }
    }

    fun createAccount() {
        val request = state.now.request.takeIf { it.isValid } ?: return
        scope.launch {
            val result = api.createUser(request) ?: return@launch
            if (result.isSuccess) {
                cred.setFromSignup(requestNow)
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
) {
    val isValid get() = request.isValid && confirmPassword == request.password
}