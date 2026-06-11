package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import koala.model.DocNode
import koala.model.DocTable
import streetlight.web.SiteDocRoute
import streetlight.web.shells.SiteDocKey
import streetlight.web.shells.siteDocShell

fun DOMRender.viewSiteDoc(node: DocNode) {
    val table = cachedTable ?: emptyList()
    shellBox(SiteDocKey.Id) {
        siteDocShell(node, table)
    }
}

fun DOMRender.viewSiteDocRoute() {
    routeBlock<SiteDocRoute, DocNode>(portal, { route ->
        if (cachedTable == null) {
            cachedTable = api.readSiteDocTable().handleResponse(toaster::toast)
        }
        api.readSiteDoc(route.docId).handleResponse(toaster::toast)
    }) { node ->
        viewSiteDoc(node)
    }
}

private var cachedTable: DocTable? = null