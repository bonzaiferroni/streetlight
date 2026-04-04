package streetlight.web.model

import kampfire.model.UserInfo
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.web.io.ApiClient

class UserGate(
    private val scope: CoroutineScope,
    val cred: UserCred,
    private val api: ApiClient,
) {
    private val state = storeOf(UserGateState())
    val stateNow get() = state.now

    val starFlow = state.flow.mapDistinct { it.user }
    val messageFlow = state.flow.mapDistinct { it.message }
    val isSignedInFlow = state.flow.mapDistinct { it.isSignedIn }

    init {
        signIn()
    }

    fun signIn() {
        if (stateNow.user != null && cred.stateNow.hasCredentials) return
        console.log("signing in")
        scope.launch {
            readUser()
        }
    }

    suspend fun readUser() {
        val user = api.readUserInfo()
        if (user != null) {
            console.log("signed in")
            state.set { it.copy(user = user) }
        } else {
            console.log("unable to sign in")
            state.set { it.copy(message = "Unable to sign in.")}
        }
    }

    fun signOut() {
//        userCache.reset()
        cred.setStayLoggedIn(false)
        state.set { it.copy(user = null) }
    }

    fun setUpdate(user: UserInfo) {
        state.set { it.copy(user = user) }
    }
}

data class UserGateState(
    val user: UserInfo? = null,
    val message: String? = null,
) {
    val isSignedIn get() = user != null
}
