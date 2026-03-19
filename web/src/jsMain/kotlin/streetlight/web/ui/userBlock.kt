package streetlight.web.ui

import kampfire.model.UserInfo
import koala.css.ModifierSet
import koala.css.Width100
import koala.css.applyModifiers
import koala.dom.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.js.div
import streetlight.web.AccountRoute
import streetlight.web.model.Streetlight

fun RenderContext.userBlock(
    app: Streetlight,
    redirect: Boolean = false,
    modifiers: ModifierSet? = null,
    block: RenderContext.(UserInfo) -> Unit
) {
    val gate = app.gate
    val portal = app.portal

    flowBlock(gate.userFlow, modifiers) { user ->
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

    renderScope.launch {
        gate.userFlow
            .filterNotNull()
            .first()
            .let {
                portal.go(currentRoute)
            }
    }

    portal.go(AccountRoute)
}