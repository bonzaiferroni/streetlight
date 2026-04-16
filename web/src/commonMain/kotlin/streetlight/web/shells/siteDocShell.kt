package streetlight.web.shells

import koala.SvgFile
import koala.css.*
import koala.html.*
import koala.model.DocNode
import koala.model.DocTable
import kotlinx.html.FlowContent
import streetlight.web.SiteDocRoute
import streetlight.web.pages.appFooter

fun FlowContent.siteDocShell(node: DocNode, table: DocTable) {
    column {
        div {
            filigree {
                heading1(node.doc.title, modify(Shrinkable))
            }
        }

        row(modify(AlignItemsStart)) {
            card(modify(Width32, ZenCardBg)) {
                setId(SiteDocKey.TableId)
                siteDocTable(table)
            }
            siteDocContent(node)
        }

        appFooter("web/src/commonMain/kotlin/streetlight/web/shells/siteDocShell.kt")
    }
}

fun FlowContent.siteDocTable(table: DocTable) {
    table.forEach { item ->
        navigation(SiteDocRoute(item.docId)) {
            textBlock(item.label, modify(Padding1))
        }
        item.children?.let {
            column(modify(PaddingLeft3)) {
                siteDocTable(it)
            }
        }
    }
}

fun FlowContent.siteDocContent(node: DocNode) {
    val doc = node.doc
    column(SiteDocKey.ContentId, modify(Flex1)) {
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
    }
}

object SiteDocKey {
    val ContentId = Id("site-doc-content")
    val TableId = Id("site-doc-table")
}