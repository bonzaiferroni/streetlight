package koala.html

import koala.SvgFile
import koala.modifier.GlowBackground
import koala.modifier.Height3
import koala.modifier.ModifierSet
import koala.modifier.modify
import kotlinx.html.FlowContent

fun FlowContent.iconLogo(mod: ModifierSet? = modify(Height3)) {
    icon(SvgFile.Flame, modify(mod, GlowBackground))
}