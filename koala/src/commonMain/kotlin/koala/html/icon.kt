package koala.html

import koala.css.CustomProperties
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.UrlValue
import koala.css.applyModifiers
import koala.css.modify
import koala.css.styleOf
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style

fun FlowContent.icon(
    src: String,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    div {
        applyModifiers(modify(ElementClass.icon, modifiers))
        style = styleOf(CustomProperties.maskSrc, UrlValue(src))
        block?.invoke(this)
    }
}