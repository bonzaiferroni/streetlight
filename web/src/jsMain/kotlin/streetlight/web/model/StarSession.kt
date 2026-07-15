package streetlight.web.model

import kampfire.model.handleResponse
import koala.model.tap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Star
import streetlight.web.io.ApiClient
import kotlin.time.Clock
import kotlin.time.Instant

class StarSession(
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(StarSessionState())
    val stateNow get() = state.now

    val starFlow = state.flow.tap { it.star }
    val signedInFlow = state.flow.tap { it.isSignedIn }
    val signedOutAtFlow = state.flow.tap { it.signedOutAt }

    fun signIn(onMessage: ((String) -> Unit)?) {
        if (stateNow.star != null) return
        console.log("signing in")
        scope.launch {
            readUser(onMessage)
        }
    }

    suspend fun readUser(onMessage: ((String) -> Unit)?) {
        api.validateLogin().handleResponse(onMessage ?: { console.log(it) }) { star ->
            console.log("signed in")
            state.set { it.copy(star = star) }
        }
    }

    fun signOut() {
        scope.launch {
            api.logout()
            // userCache.reset()
            state.set { it.copy(star = null, signedOutAt = Clock.System.now()) }
        }
    }
}

data class StarSessionState(
    val star: Star? = null,
    val message: String? = null,
    val signedOutAt: Instant? = null,
) {
    val isSignedIn get() = star != null
}
