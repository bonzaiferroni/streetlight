package streetlight.web.view

import koala.dom.*
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.NullContent
import streetlight.model.ui.SandboxRoute
import streetlight.web.dom.layout.mainBody
import streetlight.web.model.Sandbox
import streetlight.web.model.api
import streetlight.web.model.toaster

/** The sandbox, where the [subrouter] is tried out on the [SandboxRoute]s. */
fun ViewScope.sandboxScreen() = mainBody("sand", "box", "sandboxScreen.kt") {
    val model = Sandbox(contentScope, api, toaster)
    row {
        textField(model.nameState)
        button("check", model::checkAvailability).flowIsBusy(model.isBusyState, contentScope)
    }
}

fun RouteScope.viewSandboxRoute() {
    routeBlock<SandboxRoute, NullContent> {
        sandboxScreen()
    }
}

class SandboxException : Exception("There's a snake in my boot!")
