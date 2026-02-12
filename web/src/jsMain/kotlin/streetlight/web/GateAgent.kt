package streetlight.web

import kampfire.model.User
import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GateAgent(
    val scope: CoroutineScope,
    val gate: UserGate,
    val portal: Portal,
) {
    fun checkIn(block: (User) -> Unit) {
        val user = gate.stateNow.user
        if (user != null) {
            block(user)
            return
        }

        val currentRoute = portal.stateNow.route

        scope.launch {
            gate.userFlow
                .filterNotNull()
                .first()
                .let { user ->
                    portal.go(currentRoute)
                    block(user)
                }
        }

        portal.go(AccountRoute)
    }
}

