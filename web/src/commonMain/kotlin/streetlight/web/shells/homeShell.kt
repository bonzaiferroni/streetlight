package streetlight.web.shells

import koala.LottieFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.HomeContent
import streetlight.model.ui.GalaxyFoundryRoute
import streetlight.model.ui.HomeRoute
import streetlight.web.layouts.feedSection
import streetlight.web.layouts.smallGalaxyCard
import streetlight.web.pages.appFooter
import koala.html.routeMenu
import streetlight.model.ui.PostMapRoute
import streetlight.web.pages.appHeader

fun FlowContent.homeShell(content: HomeContent) {
    column(modify(MarginTop1)) {
        appHeader(modify(Height10))

        geoMapMount(null, modify(BorderRadius2, Height48, MoonShadow, BorderSolid2Px))
        box {
            column(modify(Gap8)) {
                row(modify(JustifyContentSpaceBetween)) {
                    galaxyMenu()
                    createPostMenu(null)
                }

                galaxiesSection(content.galaxies)

                feedSection(content.feed, true)

                appFooter(HomeShell.SOURCE)
            }
        }

        routeMenu("Streetlight", HomeRoute, listOf(HomeRoute, PostMapRoute("Home")), modify(PositionSticky))
    }

    dataIsland(HomeShell.IslandId, content)
}

fun FlowContent.galaxiesSection(galaxies: List<Galaxy>) {
    section(modify(QueryContainer)) {
        column(modify(Gap0)) {
            heading2("Galaxies", SectionHeadingMod)
            textBlock(
                content = "Galaxies are Streetlight communities, each with a particular focus.",
                mod = modify(Dim, TextAlignCenter)
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
            column(modify(MarginTop4, ContainerMdMarginTop0)) {
                filigree {
                    heading4("Featured Galaxies", subHeadingMods)
                }
                row(modify(Flex1, AlignItemsCenter, JustifyContentCenter)) {
                    lottie(LottieFile.dinoLoad, modify(Width32, Aspect1))
                }
            }
        }
        row {
            spacer(modify(Flex1))
            btn("➕ Create a Galaxy", GalaxyFoundryRoute, modify(Accent))
        }
    }
}

object HomeShell {
    val IslandId = Id("home-island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/homeShell.kt"
}

val SectionHeadingMod = modify(LineHeight1, WhiteSpaceNoWrap, TextAlignCenter, MoonShadowText)
