package streetlight.web.layout

import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.ui.toRoute
import koala.component.*
import streetlight.web.model.ThemeColor
import streetlight.web.model.toButtons
import streetlight.web.model.route
import streetlight.web.model.toCells

fun FlowContent.smallGalaxyCard(galaxy: Galaxy) {
    val route = galaxy.toRoute()
    card(modify(Padding(0), OverflowClip, MoonShadow)) {
        setStyle(Css.ColorScheme.of(ThemeColor.Galaxy.cssValue))

        row(modify(Gap0, Height(16))) {
            navigation(route, Flex1) {
                containImage(galaxy.image, Size100P)
            }
            column(modify(Flex2, Height(16), Gap0)) {
                column(modify(Flex1, Padding(1))) {
                    navigation(route) {
                        heading5(galaxy.name, LineHeight1)
                    }
                    val modifiers = modify(TextSmall, FadeBottom, Flex1, MaxHeight(5)).let {
                        when (galaxy.description) {
                            null -> it.append(Italic, InkDimFg)
                            else -> it
                        }
                    }
                    textBlock(galaxy.description?.value ?: "Too mysterious for a description", modifiers)
                }
                // cellGrid(galaxy.toCells(), galaxy.toButtons(), modify(Height(5), MoonShadow))
            }
        }
    }
}

object GalaxyKey {
    val TopGalaxies = jsonAttributeOf<List<Galaxy>>("galaxies")
}