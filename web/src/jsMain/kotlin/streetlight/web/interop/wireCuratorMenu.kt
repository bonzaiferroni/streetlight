package streetlight.web.interop

import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.component.button
import koala.dom.component.grid
import koala.dom.requireClosest
import koala.dom.component.textBlock
import koala.component.progressBar
import kotlinx.css.GridTemplateColumns
import streetlight.web.interop.applyCurator
import streetlight.web.interop.updateMark
import web.dom.Element
import streetlight.web.component.CuratorMenu
import streetlight.web.page.PopoverId
import streetlight.web.component.configureMarkBar
import streetlight.web.component.configureMarkButton
import streetlight.web.component.configureMarkTallyText
import streetlight.web.utility.element
import streetlight.web.dom.component.popoverMenu

/** The curator menu of a post, for marking it. */
fun ViewScope.wireCuratorMenu() {
    popoverMenu(
        popoverId = PopoverId.Curator,
        transform = { it }
    ) { element ->
        val feedElement = element.requireClosest(CuratorMenu.CuratorJson)
        val curator = feedElement.requireAttribute(CuratorMenu.CuratorJson)
        lateinit var baseElement: Element

        baseElement = grid(GridTemplateColumns("auto auto auto"), modify(Padding(2), AlignItemsCenter)) {
            curator.marks.forEach { mark ->
                button(mark.name, {
                    updateMark(mark.markId, feedElement)?.let {
                        baseElement.applyCurator(it)
                    }
                }, Zen) {
                    configureMarkButton(mark)
                }
                textBlock {
                    configureMarkTallyText(mark)
                }
                val progress = curator.progressOf(mark.markId)
                progressBar(progress) {
                    configureMarkBar(mark)
                }
            }
        }
    }
}