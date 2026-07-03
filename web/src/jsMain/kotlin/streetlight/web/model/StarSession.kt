package streetlight.web.model

import kampfire.model.handleOutcome
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Star
import streetlight.web.io.ApiClient
import kotlin.time.Clock
import kotlin.time.Instant

class StarSession(
    private val scope: CoroutineScope,
    private val cred: CredentialStore,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(StarSessionState())
    val stateNow get() = state.now

    val starFlow = state.flow.mapDistinct { it.star }
    val signedInFlow = state.flow.mapDistinct { it.isSignedIn }
    val signedOutAtFlow = state.flow.mapDistinct { it.signedOutAt }
    val messageFlow = state.flow.mapDistinct { it.message }

    fun signIn() {
        if (stateNow.star != null) return
        console.log("signing in")
        scope.launch {
            readUser(true)
        }
    }

    suspend fun readUser(showToast: Boolean) {
        val star = api.validateLogin().handleOutcome({
            if (showToast) toaster.toast(it)
        })
        if (star != null) {
            console.log("signed in")
            state.set { it.copy(star = star) }
        } else {
            console.log("unable to sign in")
            state.set { it.copy(message = "Unable to sign in.")}
        }
    }

    fun signOut() {
        scope.launch {
            api.logout()
            // userCache.reset()
            state.set { it.copy(star = null, signedOutAt = Clock.System.now()) }
        }
    }

    fun setUpdate(star: Star) {
        state.set { it.copy(star = star) }
    }
}

data class StarSessionState(
    val star: Star? = null,
    val message: String? = null,
    val signedOutAt: Instant? = null,
) {
    val isSignedIn get() = star != null
}
