package koala.html

import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.buttonPopover(
    label: String,
    modifiers: ModifierSet? = null,
    popoverModifiers: ModifierSet? = null,
    emoji: String = "☰",
    id: Id = Id("${label.lowercase().replace(" ", "-")}-popover"),
    block: DIV.() -> Unit = {}
) {
    val anchor = id.toPositionAnchor()

    popover(id, anchor, modify(popoverModifiers, Magic, SlideUp)) {
        block()
    }
    button("$emoji $label", modifiers) {
        setAnchor(anchor)
        setPopoverTarget(id)
    }
}

object ButtonPopover {
    val CardMod = modify(BlurBackdrop, PrimaryCardBg, BorderRadius4, Margin1)
}