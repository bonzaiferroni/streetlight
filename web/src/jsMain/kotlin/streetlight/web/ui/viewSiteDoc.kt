package streetlight.web.ui

import koala.css.Padding4
import koala.css.Shrinkable
import koala.css.ZenCardBg
import koala.css.modify
import koala.dom.ViewContext
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.viewContextOf
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

fun ViewContext<Streetlight>.viewSiteDoc(node: DocNode) {
    val doc = node.doc
    column {
        filigree {
            heading1(doc.title, modify(Shrinkable))
        }

        doc.sections.forEach { section ->

            section {
                section.title?.let {
                    filigree {
                        heading2(it)
                    }
                }

                card(modify(ZenCardBg, Padding4)) {
                    markdown(section.content)
                }
            }
        }

        appFooter("")
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