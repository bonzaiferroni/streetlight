package streetlight.web.pages

import koala.html.appHead
import koala.model.DocNode
import koala.model.DocTable
import kotlinx.html.HTML
import streetlight.web.shells.siteDocShell

fun HTML.siteDocPage(node: DocNode, table: DocTable, styles: String) {
    appHead("Streetlight | ${node.doc.title}", styles) {
        supportProtobuf()
        supportGeoMap()
    }
    appBody {
        siteDocShell(node, table)
    }
}