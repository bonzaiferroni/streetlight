package streetlight.web.pages

import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.DisplayFlex
import koala.css.FlipX
import koala.css.GrowText
import koala.css.Height6
import koala.css.JustifyContentCenter
import koala.css.Margin1
import koala.css.OpacitySome
import koala.css.Padding1
import koala.css.TextShadow
import koala.css.Width16
import koala.css.modify
import koala.html.IconKey
import koala.html.navigation
import koala.html.heading2
import koala.html.icon
import koala.html.logo
import koala.html.row
import kotlinx.html.FlowContent
import streetlight.web.HomeRoute


fun FlowContent.appHeader() {
    row(modify(AppHeaderKey.Height, JustifyContentCenter, Margin1)) {
        icon(SvgFile.Rays, modify(OpacitySome, IconKey.Stretch, Width16))
        navigation(HomeRoute, modify(DisplayFlex)) {
            row(modify(AlignItemsCenter)) {
                logo(modify(AppHeaderKey.Height))
                heading2("Streetlight", modify(GrowText, TextShadow))
            }
        }
        icon(SvgFile.Rays, modify(OpacitySome, IconKey.Stretch, Width16, FlipX))
    }
}

object AppHeaderKey {
    val Height = Height6
}