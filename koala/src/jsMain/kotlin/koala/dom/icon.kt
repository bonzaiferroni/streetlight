package koala.dom

import koala.Svg
import koala.css.*
import koala.html.IconStyle
import koala.html.configureIcon
import kotlinx.html.DIV
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement

fun AppendScope.icon(
    file: Svg,
    mod: ModifierSet? = IconStyle.DefaultMod,
    block: DIV.() -> Unit = {}
) = div {
    configureIcon(
        file = file,
        modifiers = mod,
        block = block
    )
}.asWeb()