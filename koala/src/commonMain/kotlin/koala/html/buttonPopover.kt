package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.buttonPopover(
    label: String,
    mod: Modifier? = null,
    popoverMod: Modifier? = null,
    flair: String = "☰",
    id: Id = Id("${label.lowercase().replace(" ", "-")}-popover"),
    block: DIV.() -> Unit = {}
) {
    popover(id, mod = modify(popoverMod, Magic, SlideUp)) {
        block()
    }
    button("$flair $label", mod) {
        setPopoverTarget(id)
    }
}

object ButtonPopover {
    val CardMod = modify(BlurBackdrop, PrimaryCardBg, BorderRadius4, Margin1)
}