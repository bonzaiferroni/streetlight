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
    modifiers: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit = {}
) = div {
    configurePopover(id, anchor, modifiers, isManual, block)
}

fun Node.showPopover() = asDynamic().showModal()
fun Node.hidePopover() = try {
    console.log("--close popover")
    asDynamic().close()
    console.log("--close popover finished")
} catch (e: Exception) {
    console.log(e.message)
}
// fun Node.togglePopover() = asDynamic().togglePopover()
fun HTMLElement.isPopoverOpen() = matches(":popover-open")