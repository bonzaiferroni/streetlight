package koala.dom.component

import kampfire.model.Tap
import koala.Svg
import koala.dom.ViewScope
import kotlinx.coroutines.launch
import web.html.HTMLDivElement
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

/** The SVG at [svgState] as a square icon in the current color, following each change. */
fun ViewScope.icon(
    svgState: Tap<Svg>,
    mod: Modifier? = IconStyle.DefaultMod,
    block: DIV.() -> Unit = {}
): HTMLDivElement {
    val element = icon(svgState.now, mod, block)
    contentScope.launch { svgState.flow.collect { element.setStyle(Css.MaskUrl.of(it)) } }
    return element
}
