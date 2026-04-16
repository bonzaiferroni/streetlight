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

fun ViewContext<Streetlight>.viewSiteDoc(node: DocNode) {
    val table = cachedTable ?: emptyList()
    shellBox(SiteDocKey.ContentId) {
        siteDocShell(node, table)
    }
}

fun ViewContext<Streetlight>.viewSiteDocRoute() {
    routeBlock<SiteDocRoute, DocNode>(model.portal, { route ->
        if (cachedTable == null) {
            cachedTable = model.client.api.readSiteDocTable().also { println(it?.size) }
        }
        model.client.api.readSiteDoc(route.docId)
    }) { node ->
        viewContextOf(model) {
            viewSiteDoc(node)
        }
    }
}

private var cachedTable: DocTable? = null