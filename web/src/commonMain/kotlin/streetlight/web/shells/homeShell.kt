package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.web.layouts.layoutGalaxyPosts
import streetlight.web.pages.appFooter

fun FlowContent.homeShell(content: HomeContent) {
    column(HomeShell.homeBoxId) {
        geoMapMount()

        // val galaxyStars = content.galaxies.map { GalaxyStar(it.pathId, it.name, it.imageUrl) }

        row(modify(JustifySpaceBetween)) {
            galaxyMenu(content.galaxies, null)
            switch("bruh", id = Id("ey"))
        }


        column {
            heading3("Galaxies")
            textBlock("Galaxies are Streetlight communities, each with a particular focus.", modify(Dim))
            content.galaxies.forEach { galaxy ->
                cardOf(galaxy)
            }
        }

        layoutGalaxyPosts(content.posts)

        appFooter()
    }
}

object HomeShell {
    val homeBoxId = Id("home-box")
    val tabsId = Id("home-tabs")
}