package streetlight.web.model

import kampfire.model.AccountType
import kampfire.model.LoginRequest
import kampfire.model.Messenger
import kampfire.model.PrintLnMessenger
import kampfire.model.toDataOr
import koala.utils.launch
import koala.model.dedup
import kampfire.model.tapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Star
import streetlight.web.io.ApiClient
import kotlin.time.Clock
import kotlin.time.Instant

class SessionGate(
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val state = storeOf(StarSessionState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val starState = state.tapOf { it.star }
    val signedInFlow = stateFlow.dedup { it.isSignedIn }
    val signedOutAtFlow = stateFlow.dedup { it.signedOutAt }

    fun signIn(receiver: Messenger?) {
        if (stateNow.star != null) return
        console.log("signing in")
        scope.launch {
            readUser(receiver)
        }
    }

    fun signIn(request: LoginRequest, receiver: Messenger) {
        scope.launch("sign-in") {
            api.login(request).toDataOr(receiver) { return@launch }
            readUser(receiver)
        }
    }

    suspend fun readUser(receiver: Messenger?) {
        val star = api.validateLogin().toDataOr(receiver ?: PrintLnMessenger) { return }
        console.log("signed in: ${star.accountType}")
        state.set { copy(star = star) }

        // guest check in
        if (star.accountType == AccountType.Guest) {
            scope.launch("StarSession > check-guest") {
                api.checkGuest()
            }
        }
    }

    fun setUser(star: Star) {
        state.set { copy(star = star) }
    }

    fun signOut() {
        scope.launch(::signOut) {
            api.logout()
            // userCache.reset()
            state.set { copy(star = null, signedOutAt = Clock.System.now()) }
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
