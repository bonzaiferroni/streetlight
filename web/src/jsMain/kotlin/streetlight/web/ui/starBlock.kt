package streetlight.web.ui

import koala.css.ModifierSet
import koala.dom.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.html.js.div
import streetlight.model.data.Star
import streetlight.model.ui.StarDashRoute
import streetlight.web.model.StarSession

fun ViewScope.starBlock(
    redirect: Boolean = false,
    mod: ModifierSet? = null,
    block: ViewScope.(Star) -> Unit
) {
    val gate = app.get<StarSession>()

    flowBlock(gate.starFlow, mod) { user ->
        if (user != null) {
            block(user)
        } else {
            div {
                +"Must be signed in"
            }
        }
    }

    if (!redirect) return

    val currentRoute = portal.stateNow.route

    parentScope.launch {
        gate.starFlow
            .filterNotNull()
            .first()
            .let {
                portal.go(currentRoute)
            }
    }

    portal.go(StarDashRoute)
}