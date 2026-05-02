@file:Suppress("CssInvalidPropertyValue", "CssInvalidFunction")

package koala.html

import koala.css.Class
import koala.css.ModifierSet
import koala.css.PositionAnchor
import koala.css.Property
import koala.css.addModifiers
import koala.css.setStyle
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.popover(
    id: Id,
    anchor: PositionAnchor?,
    modifiers: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    div {
        configurePopover(id, anchor, modifiers, isManual, block)
    }
}

fun DIV.configurePopover(
    id: Id,
    anchor: PositionAnchor?,
    modifiers: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    addModifiers(PopoverKey.Class, modifiers)
    setId(id)
    anchor?.let {
        setStyle(
            Property.PositionAnchor.to(anchor)
        )
    }
    setAttribute(Attribute.Popover.to(if (isManual) "manual" else "auto"))
    block()
}

// Called on the parent element
fun CommonAttributeGroupFacade.popoverContainer(anchor: PositionAnchor) {
    setStyle(Property.AnchorName.to(anchor.containerPosition()))
}

private fun PositionAnchor.containerPosition(): PositionAnchor = PositionAnchor("${this.identifier}-container")

object PopoverKey {
    val Class = Class("popover")
    val TargetAction = Attribute<String>("popovertargetaction")
}

// language="CSS"
val PopoverCss get() = """
${PopoverKey.Class} {
    position: fixed;
    inset: auto;
    top: anchor(bottom);
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