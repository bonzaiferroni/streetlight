package streetlight.web.shell

import koala.LottieFile
import koala.modifier.*
import kotlinx.html.FlowContent
import streetlight.model.data.Galaxy
import streetlight.model.data.HomeContent
import streetlight.model.ui.GalaxyFoundryRoute
import streetlight.model.ui.HomeRoute
import streetlight.web.layout.feedSection
import streetlight.web.layout.smallGalaxyCard
import streetlight.web.page.appFooter
import streetlight.web.page.appHeader
import koala.component.*
import koala.modifier.Width
import koala.page.*
import kotlinx.css.pct
import streetlight.model.ui.SandboxIndexRoute
import streetlight.model.ui.SandboxRoute
import streetlight.web.component.connectPicker
import streetlight.web.component.createPostMenu
import streetlight.web.component.galaxyMenu

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

                leadingSection(content.galaxies)

                feedSection(content.feed) { HomeRoute(it) }

                appFooter(HomeShell.SOURCE)
            }
        }
    }

    dataIsland(HomeShell.IslandId, content)
}

fun FlowContent.leadingSection(galaxies: List<Galaxy>) {
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
                    heading4("Get Involved", subHeadingMods)
                }
                textBlock(
                    "Connect to your community through Streetlight.",
                    modify(Italic, OpacityHigh, TextAlignCenter, MarginTop(1)),
                )
                connectPicker(modify(Width(60.pct), AlignSelfCenter))
            }
        }
        row {
            spacer(Flex1)
            btn("➕ Create a Galaxy", GalaxyFoundryRoute, Zen)
        }
    }
}

object HomeShell {
    val IslandId = Id("home-island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shell/homeShell.kt"
}

/** The heading of a page section. */
val SectionHeadingMod = modify(LineHeight1, WhiteSpaceNoWrap, TextAlignCenter, MoonShadowText)
