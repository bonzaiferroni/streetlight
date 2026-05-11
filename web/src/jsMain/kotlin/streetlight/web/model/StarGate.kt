package streetlight.web.model

import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Star
import streetlight.web.io.ApiClient

class StarGate(
    private val scope: CoroutineScope,
    val cred: StarCred,
    private val api: ApiClient,
) {
    private val state = storeOf(StarGateState())
    val stateNow get() = state.now

    val starFlow = state.flow.mapDistinct { it.star }
    val signedInFlow = state.flow.mapDistinct { it.isSignedIn }
    val messageFlow = state.flow.mapDistinct { it.message }

    fun signIn() {
        if (stateNow.star != null && cred.stateNow.hasCredentials) return
        console.log("signing in")
        scope.launch {
            readUser()
        }
    }

    suspend fun readUser() {
        val star = api.validateLogin()
        if (star != null) {
            console.log("signed in")
            state.set { it.copy(star = star) }
        } else {
            console.log("unable to sign in")
            state.set { it.copy(message = "Unable to sign in.")}
        }
    }

    fun getUsername() = stateNow.star?.username

    fun signOut() {
        scope.launch {
            api.logout()
            // userCache.reset()
            state.set { it.copy(star = null) }
        }
    }

    fun setUpdate(star: Star) {
        state.set { it.copy(star = star) }
    }
}

data class StarGateState(
    val star: Star? = null,
    val message: String? = null,
) {
    val isSignedIn get() = star != null
}
