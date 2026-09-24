package koala.html

import koala.modifier.*
import kotlinx.html.FlowContent
import kotlinx.html.HR
import kotlinx.html.hr as hrElement

/** A horizontal rule in the current color. */
fun FlowContent.hr(
    mod: Modifier? = null,
    block: HR.() -> Unit = { }
) {
    hrElement {
        addModifiers(HrStyle.Class, mod)
        block()
    }
}

object HrStyle {
    val Class = Class("hr")
}

// language="CSS"
val HrCss = with(HrStyle) {"""
$Class {
    border: none;
    height: 2px;
    background: color-mix(in srgb, currentColor 50%, transparent);
}
"""}

