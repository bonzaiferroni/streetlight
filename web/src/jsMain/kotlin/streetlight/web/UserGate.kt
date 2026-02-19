package streetlight.web

import kampfire.api.UserApi
import kampfire.model.LoginRequest
import kampfire.model.UserInfo
import kampfire.utils.obfuscate
import koala.model.mapDistinct
import koala.model.stateOf
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.get

class UserGate(
    private val scope: CoroutineScope,
    val cred: UserCred,
    private val api: ApiClient,
) {
    private val state = stateOf(UserGateState())
    val stateNow = state.now

    val userFlow = state.flow.mapDistinct { it.user }
    val messageFlow = state.flow.mapDistinct { it.message }

    init {
        signIn()
    }

    fun signIn() {
        scope.launch {
            val user = api.readUserInfo()
            if (user != null) {
                val credState = cred.stateNow
                if (credState.stayLoggedIn) {
                    localStorage.setItem(USERNAME_KEY, credState.usernameText)
                    localStorage.setItem(PASSWORD_KEY, credState.passwordText)
                }
                state.set { it.copy(user = user) }
            } else {
                state.set { it.copy(message = "Unable to sign in.")}
            }
        }
    }

    fun signOut() {
        state.set { it.copy(user = null) }
    }
}

data class UserGateState(
    val user: UserInfo? = null,
    val message: String? = null
)

const val USERNAME_KEY = "streetlight.username"
const val PASSWORD_KEY = "streetlight.password"
const val STAY_LOGGED_KEY = "streetlight.stay_logged"