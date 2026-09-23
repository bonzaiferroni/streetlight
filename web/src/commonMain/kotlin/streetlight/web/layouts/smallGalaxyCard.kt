package streetlight.web.layouts

import koala.modifier.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.ui.toRoute

fun FlowContent.smallGalaxyCard(galaxy: Galaxy) {
    val route = galaxy.toRoute()
    card(modify(Padding(0), OverflowClip, MoonShadow)) {
        setStyle(Css.ColorScheme.of(ThemeColor.Galaxy.cssValue))

        row(modify(Gap0, Height(16))) {
            navigation(route, Flex1) {
                featureImage(galaxy.image, Size100P)
            }
            column(modify(Flex2, Height(16), Gap0)) {
                column(modify(Flex1, Padding(1))) {
                    navigation(route) {
                        heading5(galaxy.name, LineHeight1)
                    }
                    val modifiers = modify(TextSmall, FadeBottom, Flex1).let {
                        when (galaxy.description) {
                            null -> it.append(Italic, InkDimFg)
                            else -> it
                        }
                    }
                    textBlock(galaxy.description?.value ?: "Too mysterious for a description", modifiers)
                }
                cellGrid(galaxy.cells, entityButtonsOf(galaxy, false), modify(Height(5), MoonShadow))
            }
        }
    }
}

object GalaxyKey {
    val TopGalaxies = jsonAttributeOf<List<Galaxy>>("galaxies")
}