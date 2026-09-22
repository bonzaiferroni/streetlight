package streetlight.web.shells

import koala.LottieFile
import koala.modifier.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.HomeContent
import streetlight.model.ui.GalaxyFoundryRoute
import streetlight.web.layouts.feedSection
import streetlight.web.layouts.smallGalaxyCard
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader

fun FlowContent.homeShell(content: HomeContent) {
    column(MarginTop(1)) {
        appHeader(Height(10))

        geoMapMount(null, modify(BorderRadius2, Height(48), MoonShadow, BorderSolid2Px))
        box {
            column(Gap(8)) {
                row(JustifyContentSpaceBetween) {
                    galaxyMenu()
                    createPostMenu(null)
                }

                galaxiesSection(content.galaxies)

                feedSection(content.feed)

                appFooter(HomeShell.SOURCE)
            }
        }
    }

    dataIsland(HomeShell.IslandId, content)
}

fun FlowContent.galaxiesSection(galaxies: List<Galaxy>) {
    section(ContainerTypeInlineSize) {
        column(Gap0) {
            heading2("Galaxies", SectionHeadingMod)
            textBlock(
                content = "Galaxies are Streetlight communities, each with a particular focus.",
                mod = modify(InkDimFg, TextAlignCenter)
            )
        }

        column(modify(ContainerMdRow, FlexItems1)) {
            val subHeadingMods = modify(LineHeight1, OpacityHigh)
            column {
                filigree {
                    heading4("Top Galaxies", subHeadingMods)
                }
                ulist {
                    galaxies.forEach { galaxy ->
                        listItem {
                            smallGalaxyCard(galaxy)
                        }
                    }
                }
            }
            column(modify(MarginTop(4), ContainerMdMarginTop0)) {
                filigree {
                    heading4("Featured Galaxies", subHeadingMods)
                }
                row(modify(Flex1, AlignItemsCenter, JustifyContentCenter)) {
                    lottie(LottieFile.DinoLoad, modify(Width(32), Aspect1))
                }
            }
        }
        row {
            spacer(Flex1)
            btn("➕ Create a Galaxy", GalaxyFoundryRoute, Accent)
        }
    }
}

object HomeShell {
    val IslandId = Id("home-island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/homeShell.kt"
}

val SectionHeadingMod = modify(LineHeight1, WhiteSpaceNoWrap, TextAlignCenter, MoonShadowText)
