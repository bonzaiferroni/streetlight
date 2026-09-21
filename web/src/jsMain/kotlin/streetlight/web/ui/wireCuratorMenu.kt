package streetlight.web.ui

import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.button
import koala.dom.grid
import koala.dom.requireClosest
import koala.dom.textBlock
import koala.html.progressBar
import kotlinx.css.GridTemplateColumns
import streetlight.web.interop.applyCurator
import streetlight.web.interop.updateMark
import web.dom.Element

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
                }, modify(Zen)) {
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