package streetlight.web.pages

import koala.SvgFile
import koala.css.DisplayFlex
import koala.css.FlipX
import koala.css.Gap0
import koala.css.Height6
import koala.css.Height8
import koala.css.JustifyContentCenter
import koala.css.OpacityLow
import koala.css.Padding1
import koala.css.Width16
import koala.css.ZenBg
import koala.css.modify
import koala.html.IconKey
import koala.html.navigation
import koala.html.icon
import koala.html.logo
import koala.html.row
import kotlinx.html.FlowContent
import streetlight.web.HomeRoute


fun FlowContent.appHeader() {
    val rayMod = modify(OpacityLow, IconKey.Stretch, Width16)
    row(modify(Height8, JustifyContentCenter, Gap0, ZenBg, Padding1)) {
        icon(SvgFile.Rays, rayMod)
        navigation(HomeRoute, modify(DisplayFlex)) {
            logo(modify(Height6))
        }
        icon(SvgFile.Rays, rayMod + FlipX)
    }
}

object AppHeaderKey {
}