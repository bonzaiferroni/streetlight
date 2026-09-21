@file:Suppress("CssInvalidPropertyValue", "CssInvalidFunction")

package koala.html

import koala.interop.InlineJs
import koala.modifier.*
import kotlinx.html.A
import kotlinx.html.BUTTON
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.onClick

fun FlowContent.popover(
    id: Id,
    mod: ModifierSet? = null,
    anchor: PositionAnchor? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    div {
        configurePopover(id, mod, anchor, isManual, block)
    }
}

fun DIV.configurePopover(
    id: Id,
    mod: ModifierSet? = null,
    anchor: PositionAnchor? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    addModifiers(Popover.Class, mod)
    setId(id)
    setStyle(Css.PositionAnchor.of(anchor ?: id.toPositionAnchor()))
    setAttribute(Attribute.Popover.to(if (isManual) "manual" else "auto"))
    block()
}

fun FlowContent.popoverCard(
    id: Id,
    mod: ModifierSet? = null,
    content: DIV.() -> Unit = {}
) {
    popover(id, modify(Popover.CardMod, Margin1, mod)) {
        content()
    }
}

fun BUTTON.closePopoverOnClick(id: Id) {
    setPopoverTarget(id, "hide")
}

fun A.closePopoverOnClick(id: Id) {
    onClick = InlineJs.closePopover(id).block
}

object Popover {
    val Class = Class("popover")
    val TargetAction = stringAttributeOf("popovertargetaction")

    val CardMod = modify(BlurBackdrop, BorderRadius3, BorderSolid2Px, AutoMagic, Scale, OverflowClip, Padding(0))
}

// language="CSS"
val PopoverCss get() = """
${Popover.Class} {
    position: fixed;
    inset: auto;
    top: anchor(bottom);
    justify-self: anchor-center;
    border: none;
    background: none;
    color: inherit;
}

${Popover.Class}.magic {
    transition: 
        opacity 200ms ease-in-out, 
        transform 200ms ease-in-out, 
        filter 200ms ease-out, 
        display 200ms allow-discrete;
}

${Popover.Class}.magic:popover-open {
    opacity: 1;
}

${Popover.Class}.magic.blur:popover-open {
    filter: blur(0px);
}

${Popover.Class}.magic.slide-down:popover-open,
${Popover.Class}.magic.slide-right:popover-open,
${Popover.Class}.magic.slide-left:popover-open,
${Popover.Class}.magic.slide-up:popover-open {
    transform: translate(0px, 0px);
}

@starting-style {
    ${Popover.Class}.magic:popover-open {
        opacity: 0;
    }
    
    ${Popover.Class}.magic.blur:popover-open {
        filter: var(--magic-blur);
    }
    
    ${Popover.Class}.magic.slide-up:popover-open {
        transform: translate(0px, 20px);
    }
    
    ${Popover.Class}.magic.slide-left:popover-open {
        transform: translate(20px, 0px);
    }
    
    ${Popover.Class}.magic.slide-right:popover-open {
        transform: translate(-20px, 0px);
    }
}
"""