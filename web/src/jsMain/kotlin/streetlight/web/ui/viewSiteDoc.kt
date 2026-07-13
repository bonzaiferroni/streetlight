package streetlight.web.ui

import kampfire.model.handleOutcome
import koala.dom.*
import koala.model.DocNode
import koala.model.DocTable
import streetlight.model.ui.SiteDocRoute
import streetlight.web.shells.SiteDocKey
import streetlight.web.shells.siteDocShell

fun AppScope.viewSiteDoc(node: DocNode) {
    val table = cachedTable ?: emptyList()
    shellBox(SiteDocKey.Id, hookInitializers) {
        siteDocShell(node, table)
    }
}

fun AppScope.viewSiteDocRoute() {
    routeBlock<SiteDocRoute, DocNode>(portal, { route ->
        if (cachedTable == null) {
            cachedTable = api.readSiteDocTable().handleOutcome(toaster::toast)
        }
        api.readSiteDoc(route.docId).handleOutcome(toaster::toast)
    }) { node ->
        viewSiteDoc(node)
    }
}

private var cachedTable: DocTable? = null