package koala.html

import koala.SvgFile
import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style

fun FlowContent.icon(
    file: SvgFile,
    modifiers: ModifierSet? = null,
    block: (DIV.() -> Unit)? = null
) {
    div {
        applyModifiers(modify(ElementClass.icon, modifiers))
        applyStyles(styleOf(StyleProperty.maskSrc to UrlValue(file.path)))
        block?.invoke(this)
    }
}