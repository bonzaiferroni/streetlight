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
        row(modify(Gap0, Height16)) {
            navigation(route, modify(Flex1)) {
                featureImage(galaxy.images?.small, modify(Size100P))
            }
            column(modify(Flex2, Height16, Gap0)) {
                column(modify(Flex1, Padding1)) {
                    navigation(route) {
                        heading4(galaxy.name, modify(LineHeight1))
                    }
                    val modifiers = modify(SmallText, FadeBottom, Flex1).let {
                        when (galaxy.description) {
                            null -> it + Italic + Dim
                            else -> it
                        }
                    }
                    textBlock(galaxy.description ?: "Too mysterious for a description", modifiers)
                }
                row(modify(Height5, FlexItems1, GapTiny, MoonShadow)) {
                    val cardMods = modify(BorderRadius0, JustifyContentCenter, AlignItemsCenter)
                    card(modify(cardMods)) {
                        row {
                            textBlock("events:", modify(Dim))
                            textBlock((0..10).random().toString())
                        }
                    }
                    cell {
                        galaxyLightCell(galaxy.lightCount, galaxy.galaxyId)
                    }
                }
            }
        }
    }
}

object GalaxyKey {
    val TopGalaxies = jsonAttributeOf<List<Galaxy>>("galaxies")
}