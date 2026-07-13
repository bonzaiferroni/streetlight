package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.navigation
import koala.html.icon
import koala.html.logo
import koala.html.row
import kotlinx.html.FlowContent
import streetlight.model.ui.HomeRoute


fun FlowContent.appHeader(
    height: Modifier = Height8
) {
    val rayMod = modify(Aspect3By2, Flex1, MaxWidth24)
    row(modify(height, JustifyContentCenter, Gap0, Padding1, AlignItemsCenter)) {
        icon(SvgFile.CircularFiligreeAnimated, rayMod + FlipX)
        navigation(HomeRoute, modify(DisplayFlex)) {
            logo(modify(height, FocusTarget))
        }
        icon(SvgFile.CircularFiligreeAnimated, rayMod)
    }
}

object AppHeaderKey {
}