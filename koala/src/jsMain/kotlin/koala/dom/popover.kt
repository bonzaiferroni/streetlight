package koala.dom

import koala.modifier.*
import koala.html.AppRoute
import koala.html.Id
import koala.html.Popover
import koala.html.configurePopover
import kotlinx.css.pct
import kotlinx.html.DIV
import kotlinx.html.js.div
import web.dom.Element
import web.html.HTMLElement

fun AppendScope.popoverRaw(
    id: Id,
    mod: Modifier = Padding(1),
    anchor: PositionAnchor? = null,
    isManual: Boolean = false,
    config: DIV.() -> Unit = {}
) = div {
    configurePopover(id, mod, anchor, isManual, config)
}

fun AppendScope.popover(
    id: Id,
    mod: Modifier? = null,
    anchor: PositionAnchor? = null,
    isManual: Boolean = false,
    content: DIV.() -> Unit = {}
) = popoverRaw(id, Padding(1), anchor, isManual) {
    popoverCard(mod) {
        content()
    }
}

fun AppendScope.popoverCard(
    mod: Modifier? = null,
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
    label, modify(OpacityHigh, TextSmall, TextAlignCenter, Padding(1), TextUppercase)
)

fun AppendScope.popoverOption(label: String, mod: Modifier? = null, onClick: () -> Unit) =
    button(onClick, mod = modify(mod, Padding(1), MinWidth(16))) {
        textBlock(label, modify(TextAlignCenter, Width(100.pct)))
    }

fun AppendScope.popoverOption(option: MenuAction) = popoverOption(option.label, option.mod, option.onClick)

fun AppendScope.popoverOption(route: AppRoute, label: String = route.label, mod: Modifier? = null) =
    navigation(route, mod = modify(mod, Padding(1), MinWidth(16))) {
        textBlock(label, modify(TextAlignCenter, Width(100.pct)))
    }

fun AppendScope.popoverOption(option: MenuRoute) =
    popoverOption(option.route, option.label, option.mod)