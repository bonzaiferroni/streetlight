package streetlight.web.shells

import koala.LottieFile
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.Post
import streetlight.web.CreateGalaxyRoute
import streetlight.web.EarthMapRoute
import streetlight.web.layouts.layoutEventPosts
import streetlight.web.layouts.smallGalaxyCard
import streetlight.web.pages.appFooter
import streetlight.web.ui.EarthKey

fun FlowContent.homeShell(content: HomeContent) {
    column(HomeShellKey.ContainerId) {
        geoMapMount(null, modify(BorderRadius2, Height48, MoonShadow))
        box {
            btn("View Map", EarthMapRoute(null), EarthKey.ViewMapButtonMod)
            column(modify(Gap8)) {
                row(modify(JustifyContentSpaceBetween)) {
                    galaxyMenu(content.galaxies, null)
                    // switch("bruh", id = Id("bruh"))

                    spacer()
                }

                galaxiesSection(content.galaxies)

                layoutPosts(content.posts)
                // layoutEventPosts("Upcoming Events", content.posts)

                section {
                    filigree {
                        heading2("Lit Events", SectionHeadingMod)
                    }

                    card(modify(Height32, MoonShadow, Padding0)) {
                        swapBlock(HomeShellKey.LightSwapId, modify(Magic)) {
                            column(modify(JustifyContentCenter, Dim, Gap0)) {
                                setId(HomeShellKey.LightInfoId)
                                setReveal(true)

                                row(modify(JustifyContentCenter)) {
                                    textBlock("Events that you")
                                    icon(SvgFile.Light)
                                    textBlock("will appear here.")
                                }
                                textBlock("This is saved on your device, unless you sign in.", modify(TextAlignCenter))
                            }
                            box(HomeShellKey.LitEventsId) {
                                setReveal(false)
                            }
                        }
                    }
                }

                appFooter(HomeShellKey.SOURCE)
            }
        }
    }
}

fun FlowContent.galaxiesSection(galaxies: List<Galaxy>) {
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
            btn("➕ Create a Galaxy", CreateGalaxyRoute, modify(Accent))
        }
    }
}

object HomeShellKey {
    val ContainerId = Id("home-box")
    val LightSwapId = Id("light-swap")
    val LightInfoId = Id("light-info")
    val LitEventsId = Id("lit-events")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/homeShell.kt"
}

val SectionHeadingMod = modify(LineHeight1, WhiteSpaceNoWrap, TextAlignCenter, MoonShadowText)

data class HomeContent(
    val galaxies: List<Galaxy>,
    val posts: List<Post>
)