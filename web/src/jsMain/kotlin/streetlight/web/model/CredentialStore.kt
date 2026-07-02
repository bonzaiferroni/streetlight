package streetlight.web.model

import kampfire.model.LoginRequest
import kampfire.model.SignUpRequest
import kampfire.utils.obfuscate
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.browser.localStorage
import org.w3c.dom.get

class CredentialStore {
    private val state = storeOf(UserCredState(
        usernameText = localStorage[USERNAME_KEY] ?: "",
        stayLoggedIn = localStorage[STAY_LOGGED_KEY]?.toBooleanStrictOrNull() ?: false,
    ))
    val stateNow get() = state.now

    val usernameFlow = state.flow.mapDistinct { it.usernameText }
    val passwordFlow = state.flow.mapDistinct { it.passwordText }
    val stayLoggedInFlow = state.flow.mapDistinct { it.stayLoggedIn }

    fun setUsername(username: String) {
        state.set { it.copy(usernameText = username) }
    }

    fun setPassword(password: String) {
        state.set { it.copy(passwordText = password) }
    }

    fun setStayLoggedIn(value: Boolean) {
        localStorage.setItem(STAY_LOGGED_KEY, value.toString())
        state.set { it.copy(stayLoggedIn = value) }
    }

    fun setFromSignup(requestNow: SignUpRequest) {
         state.set { it.copy(passwordText = requestNow.password, usernameText = requestNow.username.value) }
    }

    fun getLoginRequest(): LoginRequest? {
        val usernameOrEmail = stateNow.usernameText.takeIf { it.isNotBlank() } ?: return null
        val password = stateNow.passwordText.takeIf { it.isNotBlank() }?.obfuscate() ?: return null
        val stayLoggedIn = state.now.stayLoggedIn
        return LoginRequest(
            usernameOrEmail = usernameOrEmail,
            isTemp = !stayLoggedIn,
            password = password,
        )
    }

    fun followUpAuth(isSuccess: Boolean) {
        if (isSuccess && stateNow.stayLoggedIn) {
            localStorage.setItem(USERNAME_KEY, stateNow.usernameText)
        }
        state.set { it.copy(passwordText = "") }
    }
}

data class UserCredState(
    val usernameText: String = "",
    val passwordText: String = "",
    val stayLoggedIn: Boolean,
) {
    val hasCredentials get() = usernameText.isNotBlank() && passwordText.isNotBlank()
}

private const val USERNAME_KEY = "streetlight.username"
private const val STAY_LOGGED_KEY = "streetlight.stay_logged"
