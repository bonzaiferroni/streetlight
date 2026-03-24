@file:Suppress("CssInvalidPropertyValue")

package koala.html

import koala.css.Css
import koala.css.DisplayNone
import koala.css.ModifierSet
import koala.css.PositionAnchor
import koala.css.StyleProperty
import koala.css.Width100
import koala.css.modify
import koala.css.setModifiers
import koala.css.setStyle
import koala.css.setStylesheet
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
    setStylesheet(POPOVER_STYLES)
    div {
        setModifiers(PopoverElement.cssClass, modifiers)
        setId(id)
        setStyle(
            StyleProperty.positionAnchor.to(anchor),
            StyleProperty.anchorId.to(anchor),
            StyleProperty.containerAnchorId.to(anchor.containerVariant()),
        )
        setAttribute(TagAttribute.popover.to(if (isManual) "manual" else "auto"))
        block()
    }
}

// Called on the parent element
fun CommonAttributeGroupFacade.popoverContainer(anchor: PositionAnchor) {
    setStyle(StyleProperty.anchorName.to(anchor.containerVariant()))
}

// Derived automatically — no extra param needed
fun PositionAnchor.containerVariant(): PositionAnchor = PositionAnchor("${this.identifier}-container")

object PopoverElement {
    val cssClass = Css("popover")
}

// language="CSS"
const val POPOVER_STYLES = """
.popover {
    position: absolute;
    inset: auto;
    top: calc(anchor(var(--anchor-id) bottom) + .5rem);
    left: anchor(var(--anchor-id) left);
    max-width: anchor-size(var(--anchor-width-id) width);
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