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
    mod: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    div {
        configurePopover(id, anchor, mod, isManual, block)
    }
}

fun DIV.configurePopover(
    id: Id?,
    anchor: PositionAnchor?,
    mod: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    addModifiers(Popover.Class, mod)
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

object Popover {
    val Class = Class("popover")
    val TargetAction = stringAttributeOf("popovertargetaction")
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