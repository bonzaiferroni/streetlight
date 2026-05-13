package streetlight.web.model

import koala.model.Portal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import streetlight.model.data.Star
import streetlight.web.StarDashRoute

class GateAgent(
    val scope: CoroutineScope,
    val gate: UserGate,
    val portal: Portal,
) {
    @Deprecated("use userContent")
    fun checkIn(block: (Star) -> Unit) {
        val user = gate.stateNow.star
        if (user != null) {
            block(user)
            return
        }

        val currentRoute = portal.stateNow.route

        scope.launch {
            gate.starFlow
                .filterNotNull()
                .first()
                .let { user ->
                    portal.go(currentRoute)
                    // this seems flawed, will try to create blocks after tags are closed
                    block(user)
                }
        }

        portal.go(StarDashRoute)
    }
}

