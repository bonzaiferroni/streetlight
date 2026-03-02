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

fun RenderContext.userContent(
    app: AppContext,
    redirect: Boolean = false,
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

    if (!redirect) return

    val currentRoute = portal.stateNow.route

    val element = div {
        applyModifiers(Width100, modifiers)
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