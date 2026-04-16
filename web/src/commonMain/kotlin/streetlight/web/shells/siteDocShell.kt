package streetlight.web.shells

import koala.SvgFile
import koala.css.*
import koala.html.*
import koala.model.DocNode
import kotlinx.html.FlowContent
import streetlight.web.SiteDocRoute
import streetlight.web.pages.appFooter

fun FlowContent.siteDocShell(node: DocNode) {
    val doc = node.doc
    column(SiteDocKey.id) {
        filigree {
            heading1(doc.title, modify(Shrinkable))
        }

        column(modify(Gap8)) {
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
        }
        row {
            node.previous?.let {
                navigation(SiteDocRoute(it.docId)) {
                    row(modify(Height5, AlignItemsCenter)) {
                        icon(SvgFile.ArrowLeft, modify(AlignSelfStretch))
                        textBlock(it.label)
                    }
                }
            }
            spacer(modify(Flex1))
            node.next?.let {
                navigation(SiteDocRoute(it.docId)) {
                    row(modify(Height5, AlignItemsCenter)) {
                        textBlock(it.label)
                        icon(SvgFile.ArrowRight, modify(AlignSelfStretch))
                    }
                }
            }
        }

        appFooter("")
    }
}

object SiteDocKey {
    val id = Id("site-doc")
}