package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.PositionAnchorValue
import koala.css.StyleProperty
import koala.css.setModifiers
import koala.css.setStyle
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.popover(
    id: Id,
    anchor: PositionAnchorValue,
    modifiers: ModifierSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    div {
        setModifiers(PopoverElement.cssClass, modifiers)
        setId(id)
        setStyle(StyleProperty.positionAnchor.to(anchor))
        attributes["popover"] = if (isManual) "manual" else "auto"
        block()
    }
}

object PopoverElement {
    val cssClass = Css("popover")
}


// language="CSS"
const val POPOVER_STYLES = """
.popover {
    position: absolute;
    position-area: bottom;
    inset: auto;
}
"""