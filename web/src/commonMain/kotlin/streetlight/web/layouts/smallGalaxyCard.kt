package streetlight.web.layouts

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.web.toRoute

fun FlowContent.smallGalaxyCard(galaxy: Galaxy) {
    val route = galaxy.toRoute()
    card(modify(QueryContainer, Padding0, OverflowHidden)) {
        column(modify(ContainerTnRow, Gap0)) {
            action(route, modify(Flex1, MinHeight8)) {
                fillImage(galaxy.imageUrl, modify(Size100P))
            }
            column(modify(Flex2, Height16)) {
                column(modify(Flex1, Padding1)) {
                    action(route) {
                        heading4(galaxy.name, modify(LineHeight1))
                    }
                    val modifiers = modify(SmallText).let {
                        when (galaxy.description) {
                            null -> it + Italic + Dim
                            else -> it
                        }
                    }
                    textBlock(galaxy.description ?: "Too mysterious for a description", modifiers)
                }
                row(modify(Height5, FlexItems1, GapTiny)) {
                    val cardMods = modify(BorderRadius0, JustifyContentCenter, AlignItemsCenter)
                    card(modify(cardMods)) {
                        row {
                            textBlock("events:", modify(Dim))
                            textBlock((0..10).random().toString())
                        }
                    }
                    card(modify(cardMods)) {
                        starCell(galaxy.galaxyId)
                    }
                }
            }
        }
    }
}

fun FlowContent.starCell(galaxyId: GalaxyId) {
    row(modify(WidthAuto)) {
        setData(GalaxyKey.GalaxyStarId, galaxyId)
        textBlock((0..10).random().toString())
        icon(SvgFile.LoaderSmall, modify(Height3, AspectRatio1))
    }
}

object GalaxyKey {
    val GalaxyStarId = Attribute<GalaxyId>("galaxy-star-id")
    val TopGalaxies = Attribute<List<Galaxy>>("galaxies")
}