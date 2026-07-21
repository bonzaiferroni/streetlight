package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import koala.model.DocNode
import koala.model.DocTable
import streetlight.model.ui.SiteDocRoute
import streetlight.web.shells.SiteDocKey
import streetlight.web.shells.siteDocShell

fun ViewScope.viewSiteDoc(node: DocNode) {
    val table = cachedTable ?: emptyList()
    shellBox(SiteDocKey.Id, hookInitializers) {
        siteDocShell(node, table)
    }
}

fun ViewScope.viewSiteDocRoute() {
    // td: fix
    // routeBlock<SiteDocRoute, DocNode>(portal, { route ->
    //     if (cachedTable == null) {
    //         cachedTable = api.readSiteDocTable().handleResponse(toaster)
    //     }
    //     api.readSiteDoc(route.docId).handleResponse(toaster)
    // }) { node ->
    //     viewSiteDoc(node)
    // }
}

private var cachedTable: DocTable? = null