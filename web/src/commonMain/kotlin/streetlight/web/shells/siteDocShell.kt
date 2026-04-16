package streetlight.web.shells

import koala.css.*
import koala.html.*
import koala.model.DocNode
import kotlinx.html.FlowContent
import streetlight.web.pages.appFooter

fun FlowContent.siteDocShell(node: DocNode) {
    val doc = node.doc
    column(SiteDocKey.id, modify(Gap8)) {
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

object SiteDocKey {
    val id = Id("site-doc")
}