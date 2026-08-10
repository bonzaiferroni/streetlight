package koala.dom

import koala.css.BlurBackdrop
import koala.css.BorderRadius3
import koala.css.BorderSolid2Px
import koala.css.MarginTop1
import koala.css.ModifierSet
import koala.css.PositionAnchor
import koala.css.modify
import koala.html.Id
import koala.html.configurePopover
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

fun TagScope.popover(
    id: Id,
    anchor: PositionAnchor? = null,
    mod: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit = {}
) = div {
    configurePopover(id, anchor, mod, isManual, block)
}

fun TagScope.popoverCard(
    id: Id,
    anchor: PositionAnchor? = null,
    mod: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit = {}
) = popover(id, anchor, mod, isManual) {
    card(modify(BlurBackdrop, BorderRadius3, BorderSolid2Px, MarginTop1)) {
        block()
    }
}

fun Node.revealPopover() = asDynamic().showPopover()
fun Node.closePopover() = try {
    asDynamic().hidePopover()
} catch (e: Exception) {
    console.log(e.message)
}
fun Node.togglePopover() = asDynamic().togglePopover()
fun HTMLElement.isPopoverOpen() = matches(":popover-open")