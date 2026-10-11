package koala.dom.component

import koala.Svg
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.js.div
import koala.dom.AppendScope
import koala.dom.asWeb
import koala.component.IconStyle
import koala.component.configureIcon

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