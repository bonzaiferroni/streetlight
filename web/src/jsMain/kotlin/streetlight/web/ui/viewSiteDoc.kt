package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.card
import koala.html.filigree
import koala.html.heading1
import koala.html.heading2
import koala.html.markdown
import koala.html.section
import koala.model.DocNode
import kotlinx.html.js.section
import streetlight.web.SiteDocRoute
import streetlight.web.model.Streetlight
import streetlight.web.shells.SiteDocKey
import streetlight.web.shells.siteDocShell

fun ViewContext<Streetlight>.viewSiteDoc(node: DocNode) {
    shellBox(SiteDocKey.id) {
        siteDocShell(node)
    }
}

fun ViewContext<Streetlight>.viewSiteDocRoute() {
    routeBlock<SiteDocRoute, DocNode>(model.portal, { route ->
        model.client.api.readSiteDoc(route.docId)
    }) { node ->
        viewContextOf(model) {
            viewSiteDoc(node)
        }
    }
}