package koala.dom

import koala.css.ModifierSet
import koala.css.PositionAnchor
import koala.html.Id
import koala.html.configurePopover
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

fun TagScope.popover(
    id: Id,
    anchor: PositionAnchor?,
    mod: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit = {}
) = div {
    configurePopover(id, anchor, mod, isManual, block)
}

fun Node.showPopover() = asDynamic().showPopover()
fun Node.hidePopover() = try {
    asDynamic().hidePopover()
} catch (e: Exception) {
    console.log(e.message)
}
fun Node.togglePopover() = asDynamic().togglePopover()
fun HTMLElement.isPopoverOpen() = matches(":popover-open")