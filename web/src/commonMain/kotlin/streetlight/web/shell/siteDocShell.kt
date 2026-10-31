package streetlight.web.shell

import koala.SvgFile
import koala.modifier.*
import koala.model.DocNode
import koala.model.DocId
import koala.model.DocTable
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.li
import streetlight.model.data.DocContent
import streetlight.model.ui.SiteDocRoute
import streetlight.web.layout.pageHeader
import streetlight.web.layout.mainBody
import streetlight.web.page.menuSectionLabel
import streetlight.web.component.systemHeading
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

            div {
                addModifiers(SiteDocStyle.Container)
                card(modify(SiteDocStyle.Menu, ZenBg)) {
                    column(AlignSelfCenter) {
                        docTableMenu(table, node.doc.docId)
                        docSectionsMenu(node)
                    }
                    icon(SvgFile.CaretUp.small, modify(SmallIconHeight, SiteDocStyle.Switch))
                }
                siteDocContent(node)
            }
        }
    }
}

/** The links of [table], nested by level, with the link of [currentId] marked. */
fun FlowContent.docTableMenu(table: DocTable, currentId: DocId) {
    column(SiteDocStyle.TableId, modify(SiteDocStyle.TableMenu, Gap0)) {
        menuSectionLabel("documents", SiteDocStyle.LabelMod)
        docTableMenuItems(table, currentId)
    }
}

fun FlowContent.docTableMenuItems(table: DocTable, currentId: DocId) {
    table.forEach { item ->
        navigation(SiteDocRoute(item.docId)) {
            val current = PrimaryFg.takeIf { item.docId == currentId }
            textBlock(item.label, modify(SiteDocStyle.TableItem, SiteDocStyle.ItemMod, current))
        }
        item.children?.let {
            column(modify(PaddingLeft(3), Gap0)) {
                docTableMenuItems(it, currentId)
            }
        }
    }
}

/** The jump points of the doc of [node] as titles and target ids, led by its untitled introduction. */
fun DocNode.sectionJumps(): List<Pair<String, Id>> {
    val sections = doc.sections
    val intro = ("Introduction" to SiteDocStyle.ContentId).takeIf { sections.size > 1 && sections.first().title == null }
    return listOfNotNull(intro) + sections.mapNotNull { section ->
        section.title?.let { title -> section.id?.let { title to it } }
    }
}

/** Jump links to the [sectionJumps] of the doc of [node], when there are two or more. */
fun FlowContent.docSectionsMenu(node: DocNode) {
    val jumps = node.sectionJumps()
    if (jumps.size < 2) return
    column(Gap0) {
        menuSectionLabel("sections", SiteDocStyle.LabelMod)
        olist(Gap0) {
            jumps.forEach { (title, id) ->
                li {
                    navigation(id, modify(SiteDocStyle.SectionLink, DisplayBlock, SiteDocStyle.ItemMod)) {
                        +title
                    }
                }
            }
        }
    }
}

/** The sections of the doc of [node], with links to its neighbors. */
fun FlowContent.siteDocContent(node: DocNode) {
    val doc = node.doc
    column(SiteDocStyle.ContentId) {
        column(Gap(8)) {
            doc.sections.forEach { section ->

                section {
                    section.title?.let { title ->
                        systemHeading(title) {
                            section.id?.let {
                                setId(it)
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

object SiteDocStyle {
    val ContentId = Id("site-doc-content")
    val TableId = Id("site-doc-table")
    val Container = Class("site-doc")
    val Menu = Container.withBemElement("menu")
    val TableMenu = Container.withBemElement("table-menu")
    val TableItem = Container.withBemElement("table-item")
    val SectionLink = Container.withBemElement("section-link")
    val Switch = Container.withBemElement("switch")
    val Toggle = Container.withBemModifier("toggle")
    val ItemMod = modify(PaddingX(1), PaddingY(.5))
    val LabelMod = modify(Width100Pct, MaxWidth(22), AlignSelfCenter)
    const val MenuMinWidth = 850
}

// language="CSS"
val SiteDocCss get() = with(SiteDocStyle) { """
$Container {
    display: grid;
    gap: var(--unit);
}

$Menu { position: relative; }

$Switch {
    position: absolute;
    top: var(--unit);
    right: var(--unit);
    transition: rotate var(--magic-interval) var(--magic-easing);
}

@media (min-width: ${MenuMinWidth}px) {
    $Container {
        grid-template-columns: var(--unit-32) minmax(0, 1fr);
        align-items: start;
    }
    $Menu {
        position: sticky;
        top: var(--unit-8);
    }
    $Switch { display: none; }
}

/* the toggle shows the table menu */
@media (max-width: ${MenuMinWidth - 1}px) {
    $Container:not($Toggle) $TableMenu   { display: none; }
    $Container:not($Toggle) $Switch      { rotate: 180deg; }

    $Container$Toggle $TableItem {
        transition: var(--transition-opacity), var(--transition-transform);

        @starting-style {
            opacity: 0;
            transform: var(--slide-right-initial);
        }
    }
}
""" }
