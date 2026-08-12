package streetlight.web.model

import kampfire.api.Password
import kampfire.api.obfuscatePassword
import kampfire.model.LoginRequest
import koala.model.mutableTapOf
import koala.model.storeOf
import kotlinx.browser.localStorage
import org.w3c.dom.get

class CredentialStore {
    private val state = storeOf(UserCredState(
        usernameText = localStorage[USERNAME_KEY] ?: "",
        stayLoggedIn = localStorage[STAY_LOGGED_KEY]?.toBooleanStrictOrNull() ?: false,
    ))
    val stateNow get() = state.now

    val usernameField = state.mutableTapOf({ it.usernameText }) { copy(usernameText = it) }
    val passwordField = state.mutableTapOf({ it.passwordText }) { copy(passwordText = it) }
    val stayLoggedInField = state.mutableTapOf({ it.stayLoggedIn }) { value ->
        localStorage.setItem(STAY_LOGGED_KEY, value.toString())
        copy(stayLoggedIn = value)
    }

    fun getLoginRequest(): LoginRequest? {
        val usernameOrEmail = stateNow.usernameText.takeIf { it.isNotBlank() } ?: return null
        val password = stateNow.passwordText.takeIf { it.isNotBlank() }?.let { Password(it).obfuscatePassword() }
            ?: return null
        val stayLoggedIn = state.now.stayLoggedIn
        return LoginRequest(
            loginIdentity = usernameOrEmail,
            isTemp = !stayLoggedIn,
            password = password,
        )
    }

    fun followUpAuth(isSuccess: Boolean) {
        if (isSuccess && stateNow.stayLoggedIn) {
            localStorage.setItem(USERNAME_KEY, stateNow.usernameText)
        }
        state.set { copy(passwordText = "") }
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
