package streetlight.web.view

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.page.shellBox
import koala.dom.getElementOrNullById
import koala.modifier.setAttribute
import kotlinx.coroutines.flow.drop
import streetlight.model.data.OnboardType
import streetlight.model.ui.ConnectRoute
import streetlight.web.dom.page.applyTheme
import streetlight.web.model.portal
import streetlight.web.page.AppAttribute
import streetlight.web.shell.ConnectShell
import streetlight.web.shell.connectShell
import streetlight.web.utility.setTitle
import web.dom.document

/** The connect page, opened on [type], following the route as its type changes. */
fun ViewScope.viewConnect(type: OnboardType) {
    shellBox {
        connectShell(type)
    }

    document.setTitle(ConnectRoute(type))
    applyTheme(null)

    launchEffect {
        portal.routeState.flow.drop(1).collect { route ->
            val connectRoute = route as? ConnectRoute ?: return@collect
            val root = document.getElementOrNullById(ConnectShell.RootId) ?: return@collect

            root.setAttribute(AppAttribute.OnboardType.to(connectRoute.type))
        }
    }
}

fun RouteScope.viewConnectRoute() {
    val route = state.route as ConnectRoute
    viewConnect(route.type)
}
