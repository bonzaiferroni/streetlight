package koala.dom

import koala.modifier.MinWidth16
import koala.modifier.ModifierSet
import koala.modifier.OpacityHigh
import koala.modifier.Padding1
import koala.modifier.PositionAnchor
import koala.modifier.TextAlignCenter
import koala.modifier.TextSmall
import koala.modifier.TextUppercase
import koala.modifier.Width100Pct
import koala.modifier.append
import koala.modifier.modify
import koala.html.AppRoute
import koala.html.Id
import koala.html.Popover
import koala.html.configurePopover
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.dom.Element
import web.html.HTMLElement

fun AppendScope.popoverRaw(
    id: Id,
    mod: ModifierSet = modify(Padding1),
    anchor: PositionAnchor? = null,
    isManual: Boolean = false,
    config: DIV.() -> Unit = {}
) = div {
    configurePopover(id, mod, anchor, isManual, config)
}

fun AppendScope.popover(
    id: Id,
    mod: ModifierSet? = null,
    anchor: PositionAnchor? = null,
    isManual: Boolean = false,
    content: DIV.() -> Unit = {}
) = popoverRaw(id, modify(Padding1), anchor, isManual) {
    popoverCard(mod) {
        content()
    }
}

fun AppendScope.popoverCard(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit = {}
) = card(Popover.CardMod.append(mod)) {
    content()
}

fun Element.revealPopover() = asDynamic().showPopover()
fun Element.closePopover() = try {
    asDynamic().hidePopover()
} catch (e: Exception) {
    console.log(e.message)
}
fun Element.togglePopover() = asDynamic().togglePopover()
fun HTMLElement.isPopoverOpen() = matches(":popover-open")

fun AppendScope.popoverLabel(label: String) = textBlock(
    label, modify(OpacityHigh, TextSmall, TextAlignCenter, Padding1, TextUppercase)
)

fun AppendScope.popoverOption(label: String, mod: ModifierSet? = null, onClick: () -> Unit) =
    button(onClick, mod = modify(mod, Padding1, MinWidth16)) {
        textBlock(label, modify(TextAlignCenter, Width100Pct))
    }

fun AppendScope.popoverOption(option: MenuAction) = popoverOption(option.label, option.mod, option.onClick)

fun AppendScope.popoverOption(route: AppRoute, label: String = route.label, mod: ModifierSet? = null) =
    navigation(route, mod = modify(mod, Padding1, MinWidth16)) {
        textBlock(label, modify(TextAlignCenter, Width100Pct))
    }

fun AppendScope.popoverOption(option: MenuRoute) =
    popoverOption(option.route, option.label, option.mod)