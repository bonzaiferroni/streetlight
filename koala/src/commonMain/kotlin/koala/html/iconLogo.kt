package koala.html

import koala.SvgFile
import koala.modifier.*
import kotlinx.html.FlowContent

fun FlowContent.iconLogo(mod: Modifier? = modify(SmallIconHeight)) {
    icon(SvgFile.Flame, modify(mod, GlowBackground))
}