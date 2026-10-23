package streetlight.web.view

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.page.shellBox
import koala.dom.routeBlock
import koala.model.NullContent
import streetlight.model.ui.ContributeRoute
import streetlight.web.shell.contributeShell

fun ViewScope.viewContribute() {
    shellBox {
        contributeShell()
    }
}

fun RouteScope.viewContributeRoute() {
    routeBlock<ContributeRoute, NullContent>({ NullContent }) {
        viewContribute()
    }
}
