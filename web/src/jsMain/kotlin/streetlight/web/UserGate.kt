package streetlight.web

import kampfire.api.UserApi
import kampfire.model.LoginRequest
import kampfire.model.User
import kampfire.utils.obfuscate
import kotlinx.coroutines.launch

class UserGate(app: AppContext): BrowserModel<UserGateState>(UserGateState(), app.appScope), AppContext by app {

    val userFlow = stateFlow.mapDistinct { it.user }
    val messageFlow = stateFlow.mapDistinct { it.message }

    private var usernameOrEmail = ""
    private var password = ""

    fun setUsername(username: String) {
        usernameOrEmail = username
        setState { it.copy(usernameText = username) }
    }

    fun setPassword(password: String) {
        this.password = password
        setState { it.copy(passwordText = password) }
    }

    fun getLoginRequest() = LoginRequest(
        usernameOrEmail = usernameOrEmail,
        stayLoggedIn = true,
        password = password.obfuscate(),
    )

    fun signIn() {
        viewModelScope.launch {
            val user = get(UserApi.ReadInfo)
            if (user != null) {
                setState { it.copy(user = user) }
            } else {
                setState { it.copy(message = "Unable to sign in.")}
            }
        }
    }

    fun signOut() {
        setState { it.copy(user = null) }
    }
}

data class UserGateState(
    val user: User? = null,
    val usernameText: String = "",
    val passwordText: String = "",
    val message: String? = null,
)