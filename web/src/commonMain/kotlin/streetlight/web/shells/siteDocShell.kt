package streetlight.web.shells

import koala.SvgFile
import koala.modifier.*
import koala.html.*
import koala.model.DocNode
import koala.model.DocTable
import kotlinx.html.FlowContent
import streetlight.model.data.DocContent
import streetlight.model.ui.SiteDocRoute
import streetlight.web.ui.pageHeader
import streetlight.web.ui.mainBody

/** A site doc beside the table of all site docs. */
fun FlowContent.siteDocShell(content: DocContent) {
    val doc = content.node.doc
    val table = content.table
    val node = content.node
    mainBody("siteDocShell.kt") {
        pageHeader(doc.title, "a Streetlight doc", doc.image)

        row(AlignItemsStart) {
            card(modify(Width(32), ZenBg, Gap0, PositionSticky, Css.Top(8))) {
                setId(SiteDocKey.TableId)
                siteDocTable(table)
            }
            siteDocContent(node)
        }
    }
}

/** The links of [table], nested by level. */
fun FlowContent.siteDocTable(table: DocTable) {
    table.forEach { item ->
        navigation(SiteDocRoute(item.docId)) {
            textBlock(item.label, Padding(1))
        }
        item.children?.let {
            column(modify(PaddingLeft(3), Gap0)) {
                siteDocTable(it)
            }
        }
    }
}

/** The sections of a doc, with jump links when more than one has an id. */
fun FlowContent.siteDocContent(node: DocNode) {
    val doc = node.doc
    column(SiteDocKey.ContentId, Flex1) {
        card(modify(BorderRadius2, MoonShadow, OverflowClip, Gap0, Padding(0))) {
            // box(modify(AlignItemsEnd, Aspect2By1)) {
            //     image(doc.image, modify(Size100P, ObjectFitCover, MinHeight0))
            //     spacer(modify(GradientDarkBottom, AlignSelfStretch, VignetteOver))
            //     heading1(doc.title, modify(Shrinkable, TextAlignCenter, MoonShadowText, NightInk))
            // }
            val idSections = doc.sections.filter { it.id != null && it.title != null }
            if (idSections.size > 1) {
                row(modify(JustifyContentCenter, AlignItemsCenter, FlexWrap, PaddingX1)) {
                    textBlock("Jump to:", OpacityHigh)
                    idSections.forEach {
                        navigation(it.id!!) {
                            textBlock(it.title!!, Padding(1))
                        }
                    }
                }
            }
        }

        column(Gap(8)) {
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

                    card(modify(ZenBg, Padding(4))) {
                        markdown(section.content)
                    }
                }
            }
        }
        row {
            node.previous?.let {
                navigation(SiteDocRoute(it.docId)) {
                    row(modify(LargeIconHeight, AlignItemsCenter)) {
                        icon(SvgFile.ArrowLeft, AlignSelfStretch)
                        textBlock(it.label)
                    }
                }
            }
            spacer(Flex1)
            node.next?.let {
                navigation(SiteDocRoute(it.docId)) {
                    row(modify(LargeIconHeight, AlignItemsCenter)) {
                        textBlock(it.label)
                        icon(SvgFile.ArrowRight, AlignSelfStretch)
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