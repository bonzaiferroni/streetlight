package streetlight.web.layouts

import kampfire.model.small
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.web.toRoute

fun FlowContent.smallGalaxyCard(galaxy: Galaxy) {
    val route = galaxy.toRoute()
    card(modify(Padding0, OverflowClip, MoonShadow)) {
        setStyle(Property.ColorScheme.to(ColorScheme.Galaxy.cssValue))

        row(modify(Gap0, Height16)) {
            navigation(route, modify(Flex1)) {
                featureImage(galaxy.images?.small, modify(Size100P))
            }
            column(modify(Flex2, Height16, Gap0)) {
                column(modify(Flex1, Padding1)) {
                    navigation(route) {
                        heading5(galaxy.name, modify(LineHeight1))
                    }
                    val modifiers = modify(TextSmall, FadeBottom, Flex1).let {
                        when (galaxy.description) {
                            null -> it + Italic + Dim
                            else -> it
                        }
                    }
                    textBlock(galaxy.description?.value ?: "Too mysterious for a description", modifiers)
                }
                cellBlock(modify(Height5, MoonShadow)) {
                    cellContentOf(galaxy)()
                }
            }
        }
    }
}

object GalaxyKey {
    val TopGalaxies = jsonAttributeOf<List<Galaxy>>("galaxies")
}