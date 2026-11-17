package streetlight.web.view

import koala.dom.*
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.NullContent
import koala.modifier.modify
import kampfire.model.storeOf
import kampfire.model.toggle
import koala.SvgFile
import koala.modifier.*
import streetlight.model.ui.SandboxRoute
import streetlight.web.dom.layout.mainBody
import streetlight.web.model.Sandbox
import streetlight.web.model.api
import streetlight.web.model.toaster

/** The sandbox, where the [subrouter] is tried out on the [SandboxRoute]s. */
fun ViewScope.sandboxScreen() = mainBody("sand", "box", "sandboxScreen.kt") {
    val model = Sandbox(scope, api, toaster)
    button("check", model::checkAvailability).flowIsWorking(model.isBusyState, scope)
}

fun RouteScope.viewSandboxRoute() {
    routeBlock<SandboxRoute, NullContent> {
        sandboxScreen()
    }
}

class SandboxException : Exception("There's a snake in my boot!")
