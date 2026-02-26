package streetlight.web

import kampfire.model.UserInfo
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.dom.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.js.div

fun RenderContext.userGate(
    app: AppContext,
    modifiers: ModifierSet? = null,
    block: RenderContext.(UserInfo) -> Unit
) {
    val gate = app.gate
    val portal = app.portal

    val user = gate.stateNow.user
    if (user != null) {
        block(user)
        return
    }

    val currentRoute = portal.stateNow.route

    val element = div {
        applyModifiers(modifiers)
        +"Must be signed in"
    }

    renderScope.launch {
        gate.userFlow
            .filterNotNull()
            .first()
            .let { user ->
                portal.go(currentRoute)
                element.clear()
                createRender(element, renderScope) {
                     this@createRender.block(user)
                }
            }
    }

    portal.go(AccountRoute)

}