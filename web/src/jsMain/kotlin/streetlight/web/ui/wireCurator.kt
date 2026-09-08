package streetlight.web.ui

import kabinet.utils.toMetricString
import koala.css.Padding2
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.button
import koala.dom.getAttribute
import koala.dom.grid
import koala.dom.textBlock
import kotlinx.css.GridTemplateColumns

fun ViewScope.wireCurator() {
    popoverMenu(
        popoverId = PopoverId.Curator,
        transform = { it.getAttribute(CuratorMenu.CuratorJson) }
    ) { status ->
        grid(GridTemplateColumns("auto auto"), modify(Padding2)) {
            status.marks.forEach { feedMark ->
                val postMark = status.postMarks?.firstOrNull { it.markId == feedMark.markId }
                button {
                    textBlock(feedMark.name)
                }
                textBlock((postMark?.sum ?: 0).toMetricString())
            }
        }
    }
}