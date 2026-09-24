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
import koala.model.Portal
import koala.model.RouteInflator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Star
import streetlight.model.ui.HomeRoute
import streetlight.web.io.ApiClient
import kotlin.time.Clock
import kotlin.time.Instant

/** The signed-in star, and sign-in and sign-out. */
class SessionClient(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val inflator: RouteInflator,
    private val dataCache: DataCache,
    private val portal: Portal,
) {
    private val state = storeOf(StarSessionState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val starState = state.tapOf { it.star }
    val signedInFlow = stateFlow.dedup { it.isSignedIn }
    val signedOutAtFlow = stateFlow.dedup { it.signedOutAt }

    /** Signs in with the session cookie, when no star is signed in. */
    fun signIn(receiver: Messenger?) {
        if (stateNow.star != null) return
        console.log("signing in")
        scope.launch {
            readUser(receiver)
        }
    }

    /** Signs in with [request]. */
    fun signIn(request: LoginRequest, receiver: Messenger) {
        scope.launch("sign-in") {
            api.user.login(request).toDataOr(receiver) { return@launch }
            readUser(receiver)
        }
    }

    /** Reads the star of the current session. */
    suspend fun readUser(receiver: Messenger?) {
        val star = api.star.validateLogin().toDataOr(receiver ?: PrintLnMessenger) { return }
        console.log("signed in: ${star.accountType}")
        state.set { copy(star = star) }

        // guest check in
        if (star.accountType == AccountType.Guest) {
            scope.launch("StarSession > check-guest") {
                api.user.checkGuest()
            }
        }
    }

    fun setUser(star: Star) {
        state.set { copy(star = star) }
    }

    /** Signs out, clears the cached content and data, and goes home. */
    fun signOut() {
        println("signing out")
        scope.launch(::signOut) {
            api.user.logout()
            inflator.clear()
            dataCache.clear()
            portal.go(HomeRoute)
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
