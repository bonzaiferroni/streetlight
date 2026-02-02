package koala.html

import koala.css.CssClass
import koala.css.IconClass
import koala.css.applyModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style

fun FlowContent.icon(
    src: String,
    vararg modifiers: CssClass,
    block: (DIV.() -> Unit)? = null
) {
    div {
        applyModifiers(IconClass, *modifiers)
        style = "--mask-src: url('/www/svg/$src.svg');"
        block?.invoke(this)
    }
}