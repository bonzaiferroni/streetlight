package koala.html

import koala.SvgFile
import koala.css.GlowBackground
import koala.css.Height3
import koala.css.Height5
import koala.css.ModifierSet
import koala.css.modify
import kotlinx.html.FlowContent

fun FlowContent.iconLogo(mod: ModifierSet? = modify(Height3)) {
    icon(SvgFile.Flame, modify(mod, GlowBackground))
}