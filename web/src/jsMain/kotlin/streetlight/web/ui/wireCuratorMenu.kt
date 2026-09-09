package streetlight.web.ui

import koala.css.Padding2
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.button
import koala.dom.grid
import koala.dom.onClickElement
import koala.dom.requireAttribute
import koala.dom.requireClosest
import koala.dom.textBlock
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

        baseElement = grid(GridTemplateColumns("auto auto"), modify(Padding2)) {
            curator.marks.forEach { mark ->
                button({
                    baseElement.applyCurator(updateMark(mark.markId, feedElement))
                }) {
                    configureMarkButton(mark)
                    textBlock(mark.name)
                }
                textBlock {
                    configureMarkTallyText(mark)
                }
            }
        }
    }
}