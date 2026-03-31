@file:Suppress("CssInvalidPropertyValue")

package koala.html

import koala.css.Class
import koala.css.ModifierSet
import koala.css.Anchor
import koala.css.Property
import koala.css.addModifiers
import koala.css.setStyle
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.popover(
    id: Id,
    anchor: Anchor,
    modifiers: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    div {
        addModifiers(PopoverKey.Class, modifiers)
        setId(id)
        setStyle(
            Property.PositionAnchor.to(anchor),
            Property.AnchorId.to(anchor),
            Property.ContainerAnchorId.to(anchor.containerPosition()),
        )
        setAttribute(Attribute.Popover.to(if (isManual) "manual" else "auto"))
        block()
    }
}

// Called on the parent element
fun CommonAttributeGroupFacade.popoverContainer(anchor: Anchor) {
    setStyle(Property.AnchorName.to(anchor.containerPosition()))
}

private fun Anchor.containerPosition(): Anchor = Anchor("${this.identifier}-container")

object PopoverKey {
    val Class = Class("popover")
}

// language="CSS"
val PopoverCss get() = """
${PopoverKey.Class} {
    position: absolute;
    inset: auto;
    top: anchor(var(--anchor-id) bottom);
    left: anchor(var(--anchor-container-id) left);
    max-width: anchor-size(var(--anchor-container-id) width);
    justify-self: anchor-center;
    border: none;
    background: none;
    color: inherit;
}

${PopoverKey.Class}.magic {
    transition: 
        opacity 200ms ease-in-out, 
        transform 200ms ease-in-out, 
        filter 200ms ease-out, 
        display 200ms allow-discrete;
}

${PopoverKey.Class}.magic:popover-open {
    opacity: 1;
}

${PopoverKey.Class}.magic.blur:popover-open {
    filter: blur(0px);
}

${PopoverKey.Class}.magic.slide-down:popover-open,
${PopoverKey.Class}.magic.slide-right:popover-open,
${PopoverKey.Class}.magic.slide-left:popover-open,
${PopoverKey.Class}.magic.slide-up:popover-open {
    transform: translate(0px, 0px);
}

@starting-style {
    ${PopoverKey.Class}.magic:popover-open {
        opacity: 0;
    }
    
    ${PopoverKey.Class}.magic.blur:popover-open {
        filter: var(--magic-blur);
    }
    
    ${PopoverKey.Class}.magic.slide-up:popover-open {
        transform: translate(0px, 20px);
    }
    
    ${PopoverKey.Class}.magic.slide-left:popover-open {
        transform: translate(20px, 0px);
    }
    
    ${PopoverKey.Class}.magic.slide-right:popover-open {
        transform: translate(-20px, 0px);
    }
}
"""