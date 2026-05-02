package koala.dom

import koala.css.*
import koala.html.Id
import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

private val popoverMap = mutableMapOf<String, HTMLElement>()
private val menus = mutableMapOf<String, DOMContext.(String) -> Unit>()

fun <Context, Data> ViewContext<Context>.registerMenu(
    id: Id,
    dataOf: (String) -> Data,
    block: ViewContext<Context>.(Data) -> Unit
) {
    menus[id.identifier] = {
        val data = dataOf(it)
        val element = card(modify(BlurBackdrop, BorderRadius3))
        element.appendRender(renderScope) {
            viewContextOf(model) {
                block(data)
            }
        }
    }
}

fun callMenu(anchor: String, menu: String, data: String) {
    val menuRender = menus[menu] ?: error("menu not found: $menu")
    val anchor = PositionAnchor(anchor)
    val popover = popoverMap.getOrPut(menu) {
        val id = Id(menu)
        document.body!!.append {
            popover(id, anchor, modify(Magic, SlideUp))
        }.first()
    }

    popover.clear()
    popover.append {
        menuRender(data)
    }

    popover.setProperty(Property.PositionAnchor.to(anchor))
    popover.showPopover()
}

private class Menu(
    val render: () -> Unit
)