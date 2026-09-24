package koala.html

import koala.SvgFile
import koala.modifier.*
import kotlinx.html.FlowContent

/** The flame logo as a glowing icon. */
fun FlowContent.iconLogo(mod: Modifier? = SmallIconHeight) {
    icon(SvgFile.Flame, modify(mod, GlowBackground))
}