package koala.dom

import koala.css.AutoMagic
import koala.css.BlurBackdrop
import koala.css.BorderRadius3
import koala.css.BorderSolid2Px
import koala.css.MinWidth16
import koala.css.ModifierSet
import koala.css.OverflowClip
import koala.css.Padding0
import koala.css.Padding1
import koala.css.PositionAnchor
import koala.css.Scale
import koala.css.TextAlignCenter
import koala.css.Width100P
import koala.css.modify
import koala.html.AppRoute
import koala.html.Id
import koala.html.configurePopover
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

fun AppendScope.popover(
    id: Id,
    mod: ModifierSet? = null,
    anchor: PositionAnchor? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit = {}
) = div {
    configurePopover(id, anchor, mod, isManual, block)
}

fun AppendScope.popoverCard(
    id: Id,
    mod: ModifierSet? = null,
    anchor: PositionAnchor? = null,
    cardMod: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit = {}
) = popover(id, modify(mod, Padding1), anchor, isManual) {
    card(modify(cardMod, BlurBackdrop, BorderRadius3, BorderSolid2Px, AutoMagic, Scale, OverflowClip, Padding0)) {
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

fun AppendScope.popoverOption(label: String, mod: ModifierSet? = null, onClick: () -> Unit) =
    button(onClick, mod = modify(mod, Padding1, MinWidth16)) {
        textBlock(label, modify(TextAlignCenter, Width100P))
    }

fun AppendScope.popoverOption(option: MenuAction) = popoverOption(option.label, option.mod, option.onClick)

fun AppendScope.popoverOption(route: AppRoute, label: String = route.label, mod: ModifierSet? = null) =
    navigation(route, mod = modify(mod, Padding1, MinWidth16)) {
        textBlock(label, modify(TextAlignCenter, Width100P))
    }

fun AppendScope.popoverOption(option: MenuRoute) =
    popoverOption(option.route, option.label, option.mod)