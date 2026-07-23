package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import koala.model.DocNode
import koala.model.DocTable
import streetlight.model.data.DocContent
import streetlight.model.ui.SiteDocRoute
import streetlight.web.shells.SiteDocKey
import streetlight.web.shells.siteDocShell

fun ViewScope.viewSiteDoc(content: DocContent) {
    shellBox(SiteDocKey.Id, hookInitializers) {
        siteDocShell(content)
    }
}

fun RouteScope.viewSiteDocRoute() {
    routeBlock<SiteDocRoute, DocContent> { node ->
        viewSiteDoc(node)
    }
}
