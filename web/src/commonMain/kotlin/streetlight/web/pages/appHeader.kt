package streetlight.web.pages

import koala.modifier.*
import koala.html.hr
import koala.html.navigation
import koala.html.textLogo
import koala.html.row
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.ui.HomeRoute

fun FlowContent.appHeader(
    mod: Modifier? = modify(Height(8), Padding(1))
) {
    row(modify(mod, JustifyContentCenter, Gap(0), Padding(1), AlignItemsCenter)) {
        val rayMod = modify(Flex1, MaxWidth(24), Margin(1), MoonDropShadow, Height2Px, AspectAuto, ParticleRay)
        hr(modify(rayMod, AccentFg, FlipX))
        navigation(HomeRoute, Height(100.pct)) {
            textLogo(modify(Height(100.pct), FocusTarget))
        }
        hr(modify(rayMod, PrimaryFg))
    }
}

object AppHeaderKey {
}