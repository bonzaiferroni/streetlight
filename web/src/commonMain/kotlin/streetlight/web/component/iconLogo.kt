package streetlight.web.component

import koala.SvgFile
import koala.modifier.*
import koala.component.icon
import kotlinx.html.FlowContent

/** The flame logo as a glowing icon. */
fun FlowContent.iconLogo(mod: Modifier? = SmallIconHeight) {
    icon(SvgFile.Flame.small, modify(mod, GlowBackground))
}