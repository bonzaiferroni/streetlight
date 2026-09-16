package koala.html

import koala.modifier.Class
import koala.modifier.ModifierSet
import koala.modifier.addModifiers
import kotlinx.html.FlowContent
import kotlinx.html.HR
import kotlinx.html.hr as hrElement

fun FlowContent.hr(
    mod: ModifierSet? = null,
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

