package streetlight.web

import kampfire.api.UserApi
import kampfire.model.LoginRequest
import kampfire.model.User
import kampfire.utils.obfuscate
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import org.w3c.dom.get

class UserGate(app: AppContext): BrowserModel<UserGateState>(UserGateState(
    savePassword = localStorage[SAVE_PASSWORD_KEY]?.toBooleanStrictOrNull() ?: false,
    stayLoggedIn = localStorage[STAY_LOGGED_KEY]?.toBooleanStrictOrNull() ?: false
), app.appScope), AppContext by app {

    val userFlow = stateFlow.mapDistinct { it.user }
    val messageFlow = stateFlow.mapDistinct { it.message }

    private var usernameOrEmail = localStorage[USERNAME_KEY] ?: ""
    private var password= localStorage[PASSWORD_KEY] ?: ""

    fun setUsername(username: String) {
        usernameOrEmail = username
        setState { it.copy(usernameText = username) }
    }

    fun setPassword(password: String) {
        this.password = password
        setState { it.copy(passwordText = password) }
    }

    fun setSavePassword(value: Boolean) {
        localStorage.setItem(SAVE_PASSWORD_KEY, value.toString())
        setState { it.copy(savePassword = value) }
    }

    fun setStayLoggedIn(value: Boolean) {
        localStorage.setItem(STAY_LOGGED_KEY, value.toString())
        setState { it.copy(stayLoggedIn = value) }
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
                if (stateNow.savePassword && stateNow.stayLoggedIn) {
                    localStorage.setItem(USERNAME_KEY, usernameOrEmail)
                    localStorage.setItem(PASSWORD_KEY, password)
                }
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
    val savePassword: Boolean,
    val stayLoggedIn: Boolean
)

const val USERNAME_KEY = "streetlight.username"
const val PASSWORD_KEY = "streetlight.password"
const val SAVE_PASSWORD_KEY = "streetlight.save_password"
const val STAY_LOGGED_KEY = "streetlight.stay_logged"