package streetlight.web.pages

import koala.SvgFile
import koala.css.DisplayFlex
import koala.css.FlipX
import koala.css.Gap0
import koala.css.Height8
import koala.css.JustifyContentCenter
import koala.css.Modifier
import koala.css.OpacityLow
import koala.css.Padding1
import koala.css.Width16
import koala.css.Width24
import koala.css.modify
import koala.html.IconKey
import koala.html.navigation
import koala.html.icon
import koala.html.logo
import koala.html.row
import kotlinx.html.FlowContent
import streetlight.web.HomeRoute


fun FlowContent.appHeader(
    height: Modifier = Height8
) {
    val rayMod = modify(OpacityLow, IconKey.Stretch)
    row(modify(JustifyContentCenter, Gap0, Padding1)) {
        icon(SvgFile.Rays, rayMod)
        navigation(HomeRoute, modify(DisplayFlex)) {
            logo(modify(height))
        }
        icon(SvgFile.Rays, rayMod + FlipX)
    }
}

object AppHeaderKey {
}