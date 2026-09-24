package streetlight.web.ui

import koala.SvgFile
import koala.modifier.*
import koala.html.IconStyle
import koala.html.icon
import kotlinx.html.DIV
import kotlinx.html.FlowContent

/** A spinning icon that shows work in progress. */
fun FlowContent.workSignal(
    mod: Modifier? = IconStyle.DefaultMod,
    config: DIV.() -> Unit = { }
) {
    icon(SvgFile.CircleLoop, modify(mod, IconStyle.Signal), config)
}

object WorkSignalStyle {
    val AbsolutePositioned = modify(PositionAbsolute, Height(5), Aspect1, Margin1, PrimaryFg, FadeIn)
}