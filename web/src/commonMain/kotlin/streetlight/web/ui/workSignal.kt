package streetlight.web.ui

import koala.SvgFile
import koala.modifier.Aspect1
import koala.modifier.FadeIn
import koala.modifier.Height5
import koala.modifier.Margin1
import koala.modifier.ModifierSet
import koala.modifier.PositionAbsolute
import koala.modifier.PrimaryFg
import koala.modifier.modify
import koala.html.IconStyle
import koala.html.icon
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.workSignal(
    mod: ModifierSet? = IconStyle.DefaultMod,
    config: DIV.() -> Unit = { }
) {
    icon(SvgFile.CircleLoop, modify(mod, IconStyle.Signal), config)
}

object WorkSignalStyle {
    val AbsolutePositioned = modify(PositionAbsolute, Height5, Aspect1, Margin1, PrimaryFg, FadeIn)
}