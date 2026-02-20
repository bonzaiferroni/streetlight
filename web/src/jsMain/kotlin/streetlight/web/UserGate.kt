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
        if (!cred.stateNow.hasCredentials) return
        console.log("signing in")
        scope.launch {
            val user = api.readUserInfo()
            if (user != null) {
                state.set { it.copy(user = user) }
            } else {
                state.set { it.copy(message = "Unable to sign in.")}
            }
        }
    }

    fun signOut() {
        cred.setStayLoggedIn(false)
        state.set { it.copy(user = null) }
    }
}

data class UserGateState(
    val user: UserInfo? = null,
    val message: String? = null,
)
