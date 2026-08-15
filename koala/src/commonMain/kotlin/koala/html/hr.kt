package koala.html

import koala.css.Class
import koala.css.ModifierSet
import koala.css.addModifiers
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
    background-color: currentColor;
}
"""}

