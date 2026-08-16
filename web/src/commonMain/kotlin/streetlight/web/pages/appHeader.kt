package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.hr
import koala.html.navigation
import koala.html.icon
import koala.html.logo
import koala.html.row
import kotlinx.html.FlowContent
import streetlight.model.ui.HomeRoute

fun FlowContent.appHeader(
    height: Modifier = Height8
) {
    row(modify(height, JustifyContentCenter, Gap0, Padding1, AlignItemsCenter)) {
        val rayMod = modify(Flex1, MaxWidth24, Margin1, MoonDropShadow, Height2Px, BorderRadius1)
        hr(modify(rayMod, PrimaryFg))
        navigation(HomeRoute, modify(DisplayFlex)) {
            logo(modify(Height7, FocusTarget))
        }
        hr(modify(rayMod, AccentFg))
    }
}

object AppHeaderKey {
}