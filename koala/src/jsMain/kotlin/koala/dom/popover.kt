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
import web.dom.Element
import web.dom.Node
import web.html.HTMLElement

fun AppendScope.popoverRaw(
    id: Id,
    mod: ModifierSet = modify(Padding1),
    anchor: PositionAnchor? = null,
    isManual: Boolean = false,
    config: DIV.() -> Unit = {}
) = div {
    configurePopover(id, anchor, mod, isManual, config)
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
) = card(modify(mod, BlurBackdrop, BorderRadius3, BorderSolid2Px, AutoMagic, Scale, OverflowClip, Padding0)) {
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