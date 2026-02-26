package streetlight.web

import kampfire.model.UserInfo
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserGate(
    private val scope: CoroutineScope,
    val cred: UserCred,
    private val api: ApiClient,
    private val userCache: UserCache,
) {
    private val state = storeOf(UserGateState())
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
            handshake()
        }
    }

    suspend fun handshake() {
        val user = api.readUserInfo()
        if (user != null) {
            state.set { it.copy(user = user) }
        } else {
            state.set { it.copy(message = "Unable to sign in.")}
        }
    }

    fun signOut() {
        userCache.reset()
        cred.setStayLoggedIn(false)
        state.set { it.copy(user = null) }
    }
}

data class UserGateState(
    val user: UserInfo? = null,
    val message: String? = null,
)
