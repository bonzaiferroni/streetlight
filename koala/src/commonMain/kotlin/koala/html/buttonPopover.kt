package koala.html

import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.buttonPopover(
    label: String,
    modifiers: ModifierSet? = null,
    popoverModifiers: ModifierSet? = null,
    flair: String = "☰",
    id: Id = Id("${label.lowercase().replace(" ", "-")}-popover"),
    block: DIV.() -> Unit = {}
) {
    popover(id, mod = modify(popoverModifiers, Magic, SlideUp)) {
        block()
    }
    button("$flair $label", modifiers) {
        setPopoverTarget(id)
    }
}

object ButtonPopover {
    val CardMod = modify(BlurBackdrop, PrimaryCardBg, BorderRadius4, Margin1)
}