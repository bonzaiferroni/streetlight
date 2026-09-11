package streetlight.web.ui

import koala.SvgFile
import koala.css.Aspect1
import koala.css.FadeIn
import koala.css.Height5
import koala.css.Margin1
import koala.css.ModifierSet
import koala.css.PositionAbsolute
import koala.css.PrimaryFg
import koala.css.modify
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