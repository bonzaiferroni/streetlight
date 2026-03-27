@file:Suppress("CssInvalidPropertyValue")

package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.PositionAnchor
import koala.css.StyleProperty
import koala.css.addModifiers
import koala.css.setStyle
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.popover(
    id: Id,
    anchor: PositionAnchor,
    modifiers: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    div {
        addModifiers(PopoverElement.cssClass, modifiers)
        setId(id)
        setStyle(
            StyleProperty.positionAnchor.to(anchor),
            StyleProperty.anchorId.to(anchor),
            StyleProperty.containerAnchorId.to(anchor.containerPosition()),
        )
        setAttribute(TagAttribute.popover.to(if (isManual) "manual" else "auto"))
        block()
    }
}

// Called on the parent element
fun CommonAttributeGroupFacade.popoverContainer(anchor: PositionAnchor) {
    setStyle(StyleProperty.anchorName.to(anchor.containerPosition()))
}

private fun PositionAnchor.containerPosition(): PositionAnchor = PositionAnchor("${this.identifier}-container")

object PopoverElement {
    val cssClass = Css("popover")
}

// language="CSS"
val PopoverCss get() = """
.popover {
    position: absolute;
    inset: auto;
    top: anchor(var(--anchor-id) bottom);
    left: anchor(var(--anchor-container-id) left);
    max-width: anchor-size(var(--anchor-container-id) width);
    justify-self: anchor-center;
    margin: var(--unit-spacing);
    border: none;
    background: none;
    color: inherit;
}

.popover.magic {
    transition: 
        opacity 200ms ease-in-out, 
        transform 200ms ease-in-out, 
        filter 200ms ease-out, 
        display 200ms allow-discrete;
}

.popover.magic:popover-open {
    opacity: 1;
}

.popover.magic.blur:popover-open {
    filter: blur(0px);
}

.popover.magic.slide-up:popover-open {
    transform: translate(0px, 0px);
}

.popover.magic.slide-left:popover-open {
    transform: translate(0px, 0px);
}

@starting-style {
    .popover.magic:popover-open {
        opacity: 0;
    }
    
    .popover.magic.blur:popover-open {
        filter: var(--magic-blur);
    }
    
    .popover.magic.slide-up:popover-open {
        transform: translate(0px, 20px);
    }
    
    .popover.magic.slide-left:popover-open {
        transform: translate(20px, 0px);
    }
}
"""