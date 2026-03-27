package streetlight.web.shells

import koala.SvgFiles
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.web.layouts.layoutGalaxyPosts
import streetlight.web.pages.appFooter

fun FlowContent.homeShell(content: HomeContent) {
    column(HomeShell.homeBoxId) {
        geoMapMount()

        // val galaxyStars = content.galaxies.map { GalaxyStar(it.pathId, it.name, it.imageUrl) }

        row(modify(JustifyContentSpaceBetween)) {
            galaxyMenu(content.galaxies, null)
            switch("bruh", id = Id("ey"))
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

            card(modify(Height32, JustifyContentCenter, Dim, Gap0)) {
                row(modify(JustifyContentCenter)) {
                    textBlock("Events that you")
                    icon(SvgFiles.StarOutline)
                    textBlock("will appear here.")
                }
                textBlock("This is saved on your device, no need to sign in.", modify(TextAlignCenter))
            }
        }

        appFooter()
    }
}

object HomeShell {
    val homeBoxId = Id("home-box")
    val tabsId = Id("home-tabs")
}