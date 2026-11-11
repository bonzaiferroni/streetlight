package streetlight.web.view

import koala.dom.*
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.NullContent
import streetlight.model.ui.SandboxAccountRoute
import streetlight.model.ui.SandboxIndexRoute
import streetlight.model.ui.SandboxItemRoute
import streetlight.model.ui.SandboxItemsRoute
import streetlight.model.ui.SandboxRoute
import streetlight.model.ui.SandboxStripeRoute
import streetlight.web.dom.layout.mainBody
import streetlight.web.dom.layout.formSection

/** The sandbox, where the [subrouter] is tried out on the [SandboxRoute]s. */
fun ViewScope.viewSandbox() = mainBody("Sand", "Box", "viewSandbox.kt") {

}

fun RouteScope.viewSandboxRoute() {
    routeBlock<SandboxRoute, NullContent> {
        viewSandbox()
    }
}

class SandboxException : Exception("There's a snake in my boot!")
