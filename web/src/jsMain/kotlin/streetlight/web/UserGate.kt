package streetlight.web

import kotlinx.coroutines.CoroutineScope

class UserGate(
    scope: CoroutineScope
): BrowserModel<UserGateState>(UserGateState(), scope) {

    val userFlow = stateFlow.mapDistinct { it.user }

    fun signIn() {
        setState { it.copy(user = AppUser("wombat")) }
    }

    fun signOut() {
        setState { it.copy(user = null) }
    }
}

data class UserGateState(
    val user: AppUser? = null
)

data class AppUser(
    val username: String
)