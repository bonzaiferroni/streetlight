package streetlight.web.model

import kampfire.model.Auth
import kampfire.model.LoginRequest
import kampfire.model.SignUpRequest
import kampfire.utils.obfuscate
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.browser.localStorage
import org.w3c.dom.get

class StarCred {
    private val state = storeOf(UserCredState(
        usernameText = localStorage[USERNAME_KEY] ?: "",
        stayLoggedIn = localStorage[STAY_LOGGED_KEY]?.toBooleanStrictOrNull() ?: false,
        refreshToken = localStorage[REFRESH_TOKEN_KEY]?.takeIf { it.isNotBlank() },
        jwt = localStorage[JWT_KEY]?.takeIf { it.isNotBlank() },
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
         state.set { it.copy(passwordText = requestNow.password, usernameText = requestNow.username) }
    }

    fun getLoginRequest(): LoginRequest? {
        val usernameOrEmail = stateNow.usernameText.takeIf { it.isNotBlank() } ?: return null
        val password = stateNow.passwordText.takeIf { it.isNotBlank() }?.obfuscate()
        val refreshToken = stateNow.refreshToken.takeIf { password == null }
        val stayLoggedIn = state.now.stayLoggedIn
        return LoginRequest(
            usernameOrEmail = usernameOrEmail,
            stayLoggedIn = stayLoggedIn,
            password = password,
            refreshToken = refreshToken
        )
    }

    fun readAuth(): Auth? {
        val jwt = stateNow.jwt ?: return null
        val refreshToken = stateNow.refreshToken ?: return null
        return Auth(jwt = jwt, refreshToken = refreshToken)
    }

    fun writeAuth(auth: Auth?) {
        if (auth != null) {
            if (stateNow.stayLoggedIn) {
                localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken)
                localStorage.setItem(USERNAME_KEY, stateNow.usernameText)
                localStorage.setItem(JWT_KEY, auth.jwt)
            }
            state.set { it.copy(refreshToken = auth.refreshToken, jwt = auth.jwt, passwordText = "") }
        } else {
            localStorage.removeItem(REFRESH_TOKEN_KEY)
            localStorage.removeItem(JWT_KEY)
            state.set { it.copy(refreshToken = null, jwt = null, passwordText = "") }
        }
    }

    fun clearToken() {
        console.log("clearing token")
        localStorage.removeItem(USERNAME_KEY)
        localStorage.removeItem(REFRESH_TOKEN_KEY)
        state.set { it.copy(refreshToken = null, jwt = null) }
    }
}

data class UserCredState(
    val usernameText: String = "",
    val passwordText: String = "",
    val stayLoggedIn: Boolean,
    val refreshToken: String? = null,
    val jwt: String? = null,
) {
    val hasCredentials get() = refreshToken != null || usernameText.isNotBlank() && passwordText.isNotBlank()
}

private const val USERNAME_KEY = "streetlight.username"
private const val REFRESH_TOKEN_KEY = "streetlight.refresh"
private const val STAY_LOGGED_KEY = "streetlight.stay_logged"
private const val JWT_KEY = "streetlight.jwt"
