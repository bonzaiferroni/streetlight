package streetlight.web

import kampfire.model.LoginRequest
import kampfire.utils.obfuscate
import koala.model.mapDistinct
import koala.model.stateOf
import kotlinx.browser.localStorage
import org.w3c.dom.get

class UserCred {
    private val state = stateOf(UserCredState(
        stayLoggedIn = localStorage[STAY_LOGGED_KEY]?.toBooleanStrictOrNull() ?: false
    ))
    val stateNow get() = state.now

    val usernameFlow = state.flow.mapDistinct { it.usernameText }
    val passwordFlow = state.flow.mapDistinct { it.passwordText }
    val stayLoggedInFlow = state.flow.mapDistinct { it.stayLoggedIn }

    private var usernameOrEmail = localStorage[USERNAME_KEY] ?: ""
    private var password = localStorage[PASSWORD_KEY] ?: ""

    fun setUsername(username: String) {
        usernameOrEmail = username
        state.set { it.copy(usernameText = username) }
    }

    fun setPassword(password: String) {
        this.password = password.obfuscate()
        state.set { it.copy(passwordText = password) }
    }

    fun setStayLoggedIn(value: Boolean) {
        localStorage.setItem(STAY_LOGGED_KEY, value.toString())
        state.set { it.copy(stayLoggedIn = value) }
    }

    fun getLoginRequest(): LoginRequest? {
        val usernameOrEmail = usernameOrEmail.takeIf { it.isNotBlank() } ?: return null
        val password = password.takeIf { it.isNotBlank() } ?: return null
        val stayLoggedIn = state.now.stayLoggedIn
        return LoginRequest(
            usernameOrEmail = usernameOrEmail,
            stayLoggedIn = stayLoggedIn,
            password = password,
        )
    }
}

data class UserCredState(
    val usernameText: String = "",
    val passwordText: String = "",
    val stayLoggedIn: Boolean,
)