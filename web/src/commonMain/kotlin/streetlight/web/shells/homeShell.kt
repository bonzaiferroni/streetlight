package streetlight.web.shells

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.web.layouts.layoutGalaxyPosts
import streetlight.web.pages.appFooter

fun FlowContent.homeShell(content: HomeContent) {
    column(HomeShellKey.ContainerId) {
        geoMapMount()

        // val galaxyStars = content.galaxies.map { GalaxyStar(it.pathId, it.name, it.imageUrl) }

        row(modify(JustifyContentSpaceBetween)) {
            galaxyMenu(content.galaxies, null)
            switch("bruh", id = Id("bruh"))
        }

        section {
            column(modify(Gap0)) {
                sectionHeading(
                    labelContent = {
                        column(modify(Gap0)) {
                            centeredHeading("Galaxies", modify(Flex1))
                            textBlock(
                                content = "Galaxies are Streetlight communities, each with a particular focus.",
                                modifiers = modify(Dim, TextAlignCenter)
                            )
                        }
                    }
                )
            }

            ulist {
                content.galaxies.forEach { galaxy ->
                    listItem {
                        cardOf(galaxy)
                    }
                }
            }
        }

        layoutGalaxyPosts(content.posts)

        section {
            column(modify(Gap0)) {
                sectionHeading("Starred Events")
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

        appFooter()
    }
}

object HomeShellKey {
    val ContainerId = Id("home-box")
    val StarSwapId = Id("star-swap")
    val StarInfoId = Id("star-info")
    val StarEventsId = Id("star-events")
}