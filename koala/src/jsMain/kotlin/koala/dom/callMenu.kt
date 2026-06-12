package koala.dom

import koala.css.*
import koala.html.Id
import kotlinx.browser.document
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

private val popoverMap = mutableMapOf<String, HTMLElement>()
private val menus = mutableMapOf<String, (MenuElement) -> Unit>()

fun <Data> AppScope.registerMenu(
    id: Id,
    dataOf: (String) -> Data,
    block: AppScope.(Data) -> Unit
) {
    menus[id.identifier] = {
        val data = dataOf(it.data)
        replaceRender(it.element) {
            card(modify(BlurBackdrop, BorderRadius3)) {
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

    menuRender(MenuElement(data, popover))

    popover.setProperty(Property.PositionAnchor.to(anchor))
    popover.showPopover()
}

private data class MenuElement(val data: String, val element: HTMLElement)