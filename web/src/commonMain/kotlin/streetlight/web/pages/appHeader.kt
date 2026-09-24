package streetlight.web.pages

import koala.html.heading2
import koala.modifier.*
import koala.html.hr
import koala.html.navigation
import koala.html.textLogo
import koala.html.row
import kotlinx.css.pct
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.span
import streetlight.model.ui.HomeRoute

fun FlowContent.appHeader(
    mod: Modifier? = modify(Height(8), Padding(1))
) {
    rayHeader(mod) {
        navigation(HomeRoute, Height(100.pct)) {
            textLogo(modify(Height(100.pct), FocusTarget))
        }
    }
}

/**
 * A header of [titleFirst] and [titleSecond] as one word between rays, linking home.
 *
 * [titleFirst] takes the accent color and [titleSecond] the primary color, following the rays.
 */
fun FlowContent.configHeader(
    titleFirst: String,
    titleSecond: String,
) {
    rayHeader {
        navigation(HomeRoute) {
            heading2(mod = modify(MoonShadowText, FocusTarget)) {
                span {
                    addModifiers(AccentFg)
                    +titleFirst
                }
                span {
                    addModifiers(PrimaryFg)
                    +titleSecond
                }
            }
        }
    }
}

/** A header row of what [content] builds between two rays, the left in the accent color and the right in the primary. */
fun FlowContent.rayHeader(
    mod: Modifier? = modify(Height(8), Padding(1)),
    content: DIV.() -> Unit,
) {
    row(modify(mod, JustifyContentCenter, Gap(0), Padding(1), AlignItemsCenter)) {
        val rayMod = modify(Flex1, MaxWidth(24), Margin(1), MoonDropShadow, Height2Px, AspectAuto, ParticleRay)
        hr(modify(rayMod, AccentFg, FlipX))
        content()
        hr(modify(rayMod, PrimaryFg))
    }
}

object AppHeaderKey {
}
