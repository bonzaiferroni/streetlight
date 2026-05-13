package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.model.DocNode
import koala.model.DocTable
import koala.model.DocTableItem
import koala.model.storeOf
import streetlight.model.Api
import streetlight.web.SiteDocRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.SiteDocKey
import streetlight.web.shells.siteDocShell

fun RenderContext.viewSiteDoc(node: DocNode) {
    val table = cachedTable ?: emptyList()
    shellBox(SiteDocKey.Id) {
        siteDocShell(node, table)
    }
}

fun RenderContext.viewSiteDocRoute() {
    routeBlock<SiteDocRoute, DocNode>(portal, { route ->
        if (cachedTable == null) {
            cachedTable = api.readSiteDocTable()
        }
        api.readSiteDoc(route.docId)
    }) { node ->
        viewSiteDoc(node)
    }
}

private var cachedTable: DocTable? = null