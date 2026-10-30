package streetlight.web.page

import koala.modifier.*
import koala.component.hr
import koala.component.navigation
import koala.component.row
import koala.model.WebRoute
import kotlinx.css.pct
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.ui.HomeRoute
import streetlight.web.component.textLogo

fun FlowContent.appHeader(
    mod: Modifier? = modify(Height(8), Padding(1)),
    rayMaxWidth: Modifier? = MaxWidth(24),
    route: WebRoute = HomeRoute(),
) {
    rayHeader(mod, rayMaxWidth) {
        navigation(route, Height(100.pct)) {
            textLogo(modify(Height(100.pct), FocusTarget))
        }
    }
}

/** A header row of what [content] builds between two rays, the left in the accent color and the right in the primary. */
fun FlowContent.rayHeader(
    mod: Modifier? = modify(Height(8), Padding(1)),
    rayMaxWidth: Modifier? = MaxWidth(24),
    content: DIV.() -> Unit,
) {
    row(modify(mod, JustifyContentCenter, Gap(0), Padding(1), AlignItemsCenter)) {
        val rayMod = modify(Flex1, rayMaxWidth, Margin(1), MoonDropShadow, Height2Px, AspectAuto, ParticleRay)
        hr(modify(rayMod, AccentFg, FlipX))
        content()
        hr(modify(rayMod, PrimaryFg))
    }
}

object AppHeaderKey {
}
