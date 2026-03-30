package streetlight.web.shells

import koala.LottieFile
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.web.CreateGalaxyRoute
import streetlight.web.layouts.layoutGalaxyPosts
import streetlight.web.layouts.smallGalaxyCard
import streetlight.web.pages.appFooter

fun FlowContent.homeShell(content: HomeContent) {
    column(HomeShellKey.ContainerId, modify(Gap4)) {
        column {
            geoMapMount()

            row(modify(JustifyContentSpaceBetween)) {
                galaxyMenu(content.galaxies, null)
                switch("bruh", id = Id("bruh"))
            }
        }

        section(modify(QueryContainer)) {
            column(modify(Gap0)) {
                heading2("Galaxies", SectionHeadingMod)
                textBlock(
                    content = "Galaxies are Streetlight communities, each with a particular focus.",
                    modifiers = modify(Dim, TextAlignCenter)
                )
            }

            column(modify(ContainerMdRow, FlexItems1)) {
                val subHeadingMods = modify(LineHeight1, OpacityMost)
                column {
                    filigree {
                        heading4("Top Galaxies", subHeadingMods)
                    }
                    ulist {
                        content.galaxies.forEach { galaxy ->
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
                        lottie(LottieFile.dinoLoad, modify(Width32, AspectRatio1))
                    }
                }
            }
            row {
                spacer(modify(Flex1))
                btn("Create a Galaxy", CreateGalaxyRoute, modify(Accent))
            }
        }

        layoutGalaxyPosts(content.posts)

        section {
            filigree {
                heading2("Starred Events", SectionHeadingMod)
            }

            card(modify(Height32)) {
                swapBlock(HomeShellKey.StarSwapId, modify(Magic)) {
                    column(modify(JustifyContentCenter, Dim, Gap0)) {
                        setId(HomeShellKey.StarInfoId)
                        setReveal(true)

                        row(modify(JustifyContentCenter)) {
                            textBlock("Events that you")
                            icon(SvgFile.StarOutline)
                            textBlock("will appear here.")
                        }
                        textBlock("This is saved on your device, no need to sign in.", modify(TextAlignCenter))
                    }
                    box(HomeShellKey.StarEventsId) {
                        setReveal(false)
                    }
                }
            }
        }

        appFooter(HomeShellKey.SOURCE)
    }
}

object HomeShellKey {
    val ContainerId = Id("home-box")
    val StarSwapId = Id("star-swap")
    val StarInfoId = Id("star-info")
    val StarEventsId = Id("star-events")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/homeShell.kt"
}

val SectionHeadingMod = modify(LineHeight1, SingleLine, TextAlignCenter, TextShadow)