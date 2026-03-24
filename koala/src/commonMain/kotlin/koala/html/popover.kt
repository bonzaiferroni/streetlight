package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.PositionAnchorValue
import koala.css.StyleProperty
import koala.css.StyleSet
import koala.css.applyModifiers
import koala.css.applyStyles
import koala.css.styleOf
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.style

fun FlowContent.popover(
    id: Id,
    anchor: PositionAnchorValue,
    modifiers: ModifierSet? = null,
    styles: StyleSet? = null,
    isManual: Boolean = false,
    block: DIV.() -> Unit
) {
    div {
        applyModifiers(PopoverElement.cssClass, modifiers)
        applyId(id)
        applyStyles(styleOf(styles, StyleProperty.positionAnchor to anchor))
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