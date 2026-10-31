package streetlight.web.shell

import koala.SvgFile
import koala.modifier.*
import koala.model.DocNode
import koala.model.DocTable
import kotlinx.html.FlowContent
import streetlight.model.data.DocContent
import streetlight.model.ui.SiteDocRoute
import streetlight.web.layout.pageHeader
import streetlight.web.layout.mainBody
import koala.component.*
import koala.markdown.*
import koala.page.*

/** A site doc beside the table of all site docs. */
fun FlowContent.siteDocShell(content: DocContent) {
    val doc = content.node.doc
    val table = content.table
    val node = content.node
    mainBody("siteDocShell.kt") {
        column {
            pageHeader(doc.title, "a Streetlight doc", doc.image)

            row(AlignItemsStart) {
                column(modify(Width(32), Css.Top(8), PositionSticky)) {
                    docTableMenu(table)
                    docSectionsMenu(node)
                }
                siteDocContent(node)
            }
        }
    }
}

/** The links of [table], nested by level. */
fun FlowContent.docTableMenu(table: DocTable) {
    card(modify(ZenBg, Gap0)) {
        setId(SiteDocKey.TableId)
        docTableMenuItems(table)
    }
}

fun FlowContent.docTableMenuItems(table: DocTable) {
    table.forEach { item ->
        navigation(SiteDocRoute(item.docId)) {
            textBlock(item.label, Padding(1))
        }
        item.children?.let {
            column(modify(PaddingLeft(3), Gap0)) {
                docTableMenuItems(it)
            }
        }
    }
}

fun FlowContent.docSectionsMenu(node: DocNode) {
    val doc = node.doc
    card(modify(BorderRadius2, MoonShadow, OverflowClip, Gap0, Padding(0))) {
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
}

/** The sections of a doc, with jump links when more than one has an id. */
fun FlowContent.siteDocContent(node: DocNode) {
    val doc = node.doc
    column(SiteDocKey.ContentId, Flex1) {
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
                        icon(SvgFile.ArrowLeft.large, AlignSelfStretch)
                        textBlock(it.label)
                    }
                }
            }
            spacer(Flex1)
            node.next?.let {
                navigation(SiteDocRoute(it.docId)) {
                    row(modify(LargeIconHeight, AlignItemsCenter)) {
                        textBlock(it.label)
                        icon(SvgFile.ArrowRight.large, AlignSelfStretch)
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