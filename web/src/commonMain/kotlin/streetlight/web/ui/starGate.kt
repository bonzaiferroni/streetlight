package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent

fun FlowContent.starGate() {
    column(modify(StarGateKey.Class, modify(MinHeight5))) {
        icon(SvgFile.LoaderSmall, modify(Height100Pct, FadeLoop))
    }
}

object StarGateKey {
    val Class = Class("star-gate")
}