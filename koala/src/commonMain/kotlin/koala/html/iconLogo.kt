package koala.html

import koala.SvgFile
import koala.modifier.GlowBackground
import koala.modifier.ModifierSet
import koala.modifier.SmallIconHeight
import koala.modifier.modify
import kotlinx.html.FlowContent

fun FlowContent.iconLogo(mod: ModifierSet? = modify(SmallIconHeight)) {
    icon(SvgFile.Flame, modify(mod, GlowBackground))
}