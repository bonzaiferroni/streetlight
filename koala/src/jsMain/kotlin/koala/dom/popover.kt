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

/** A [popover] with no card, holding what [config] builds. */
fun AppendScope.popoverRaw(
    id: Id,
    mod: Modifier = Padding(1),
    anchor: PositionAnchor? = null,
    isManual: Boolean = false,
    config: DIV.() -> Unit = {}
) = div {
    configurePopover(id, mod, anchor, isManual, config)
}

/** A popover with [id], positioned against [anchor], holding a card of what [content] builds. */
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

/** A card styled for a popover. */
fun AppendScope.popoverCard(
    mod: Modifier? = null,
    content: DIV.() -> Unit = {}
) = card(Popover.CardMod.append(mod)) {
    content()
}

/** Shows this popover. */
fun Element.revealPopover() = asDynamic().showPopover()
/** Hides this popover, ignoring the error when it is not a popover. */
fun Element.closePopover() = try {
    asDynamic().hidePopover()
} catch (e: Exception) {
    console.log(e.message)
}
fun Element.togglePopover() = asDynamic().togglePopover()
fun HTMLElement.isPopoverOpen() = matches(":popover-open")

/** [label] as the small heading of a popover's entries. */
fun AppendScope.popoverLabel(label: String) = textBlock(
    label, modify(OpacityHigh, TextSmall, TextAlignCenter, Padding(1), TextUppercase)
)

/** An entry of a popover that runs [onClick]. */
fun AppendScope.popoverOption(label: String, mod: Modifier? = null, onClick: () -> Unit) =
    button(onClick, mod = modify(mod, Padding(1), MinWidth(16))) {
        textBlock(label, modify(TextAlignCenter, Width(100.pct)))
    }

/** An entry of a popover for [option]. */
fun AppendScope.popoverOption(option: MenuAction) = popoverOption(option.label, option.mod, option.onClick)

/** An entry of a popover that navigates to [route]. */
fun AppendScope.popoverOption(route: AppRoute, label: String = route.label, mod: Modifier? = null) =
    navigation(route, mod = modify(mod, Padding(1), MinWidth(16))) {
        textBlock(label, modify(TextAlignCenter, Width(100.pct)))
    }

/** An entry of a popover for [option]. */
fun AppendScope.popoverOption(option: MenuRoute) =
    popoverOption(option.route, option.label, option.mod)