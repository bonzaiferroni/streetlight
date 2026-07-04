package streetlight.web.pages

import koala.SvgFile
import koala.css.*
import koala.html.IconKey
import koala.html.div
import koala.html.navigation
import koala.html.icon
import koala.html.image
import koala.html.logo
import koala.html.row
import kotlinx.html.FlowContent
import streetlight.web.HomeRoute


fun FlowContent.appHeader(
    height: Modifier = Height8
) {
    val rayMod = modify(Aspect3By2, Height12)
    row(modify(height, JustifyContentCenter, Gap0, Padding1, AlignItemsCenter)) {
        icon(SvgFile.CircularFiligree, rayMod + FlipX)
        navigation(HomeRoute, modify(DisplayFlex)) {
            logo(modify(height))
        }
        icon(SvgFile.CircularFiligree, rayMod)
    }
}

object AppHeaderKey {
}