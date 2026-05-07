package streetlight.web.pages

import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.DisplayFlex
import koala.css.Flex1
import koala.css.FlipX
import koala.css.Gap0
import koala.css.GrowText
import koala.css.Height6
import koala.css.Height8
import koala.css.JustifyContentCenter
import koala.css.Margin1
import koala.css.MaxWidth16
import koala.css.MaxWidth32
import koala.css.OpacitySome
import koala.css.Padding1
import koala.css.TextShadow
import koala.css.Width16
import koala.css.ZenBg
import koala.css.modify
import koala.html.IconKey
import koala.html.box
import koala.html.navigation
import koala.html.heading2
import koala.html.icon
import koala.html.logo
import koala.html.row
import kotlinx.html.FlowContent
import streetlight.web.HomeRoute


fun FlowContent.appHeader() {
    val rayMod = modify(OpacitySome, IconKey.Stretch, Width16)
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