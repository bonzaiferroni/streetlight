package koala.dom

import koala.Svg
import koala.modifier.*
import koala.html.IconStyle
import koala.html.configureIcon
import kotlinx.html.DIV
import kotlinx.html.js.div

/** The SVG [file] as a square icon in the current color. */
fun AppendScope.icon(
    file: Svg,
    mod: Modifier? = IconStyle.DefaultMod,
    block: DIV.() -> Unit = {}
) = div {
    configureIcon(
        file = file,
        mod = mod,
        block = block
    )
}.asWeb()