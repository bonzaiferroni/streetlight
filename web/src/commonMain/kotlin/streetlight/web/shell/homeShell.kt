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
                    row {
                        btn("Create Galaxy", GalaxyFoundryRoute, Zen)
                        createPostMenu(null)
                    }
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
        column(modify(ContainerMdRow, FlexItems1)) {
            column {
                notedHeading("Galaxies", "Streetlight communities with a particular focus.")
                ulist(MarginTop(1)) {
                    galaxies.forEach { galaxy ->
                        listItem {
                            smallGalaxyCard(galaxy)
                        }
                    }
                }
            }
            column(modify(MarginTop(4), ContainerMdMarginTop0)) {
                notedHeading("Get Involved", "Connect to your community through Streetlight.")
                connectPicker(modify(MarginTop(1), Width(100), MaxWidth(40), AlignSelfCenter))
            }
        }
    }
}

/** A [heading] in a filigree with its [note] close below it. */
private fun FlowContent.notedHeading(heading: String, note: String) {
    column {
        filigree {
            heading4(heading, modify(LineHeight1, OpacityHigh, Bold, TextShadow))
        }
        textBlock(note, modify(Italic, OpacityHigh, TextAlignCenter, TextSmall))
    }
}

object HomeShell {
    val IslandId = Id("home-island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shell/homeShell.kt"
}

/** The heading of a page section. */
val SectionHeadingMod = modify(LineHeight1, WhiteSpaceNoWrap, TextAlignCenter, MoonShadowText)
