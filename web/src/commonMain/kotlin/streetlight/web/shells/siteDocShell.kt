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
    row(modify(AlignItemsStart, PaddingLeft1)) {
        card(modify(Width32, ZenCardBg, Gap0, PositionSticky, TopSpacing8)) {
            setId(SiteDocKey.TableId)
            siteDocTable(table)
        }
        siteDocContent(node)
    }
}

fun FlowContent.siteDocTable(table: DocTable) {
    table.forEach { item ->
        navigation(SiteDocRoute(item.docId)) {
            textBlock(item.label, modify(Padding1))
        }
        item.children?.let {
            column(modify(PaddingLeft3, Gap0)) {
                siteDocTable(it)
            }
        }
    }
}

fun FlowContent.siteDocContent(node: DocNode) {
    val doc = node.doc
    column(SiteDocKey.ContentId, modify(Flex1)) {
        card(modify(BorderRadius2, MoonShadow, OverflowClip, Gap0, Padding0)) {
            box(modify(AlignItemsEnd, AspectX2)) {
                image(doc.image, modify(Size100P, ObjectFitCover, MinHeight0))
                spacer(modify(GradientDarkBottom, AlignSelfStretch, Vignette))
                heading1(doc.title, modify(Shrinkable, TextAlignCenter, MoonShadowText, NightInk))
            }
            val idSections = doc.sections.filter { it.id != null && it.title != null }
            if (idSections.size > 1) {
                row(modify(JustifyContentCenter, AlignItemsCenter, WrapFlex, PaddingX1)) {
                    textBlock("Jump to:", modify(OpacityMost))
                    idSections.forEach {
                        navigation(it.id!!) {
                            textBlock(it.title!!, modify(Padding1))
                        }
                    }
                }
            }
        }

        column(modify(Gap8)) {
            doc.sections.forEach { section ->

                section {
                    section.title?.let { title ->
                        filigree {
                            heading2(title) {
                                section.id?.let {
                                    setId(it)
                                }
                            }
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

        appFooter("web/src/commonMain/kotlin/streetlight/web/shells/siteDocShell.kt")
    }
}

object SiteDocKey {
    val Id = Id("site-doc-viewer")
    val ContentId = Id("site-doc-content")
    val TableId = Id("site-doc-table")
}