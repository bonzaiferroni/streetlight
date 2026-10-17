package streetlight.web.view

import koala.dom.*
import koala.dom.component.*
import koala.model.NullContent
import streetlight.model.ui.OnboardRoute
import streetlight.web.dom.layout.configBody

/** The onboarding of a star as the audience the [route] names. */
context(route: OnboardRoute)
fun ViewScope.viewOnboard() = configBody("start:", route.type.label, "viewOnboard.kt") {
    textBlock("Hello ${route.type.label}")
}

fun RouteScope.viewOnboardRoute() {
    routeBlock<OnboardRoute, NullContent>() {
        viewOnboard()
    }
}
