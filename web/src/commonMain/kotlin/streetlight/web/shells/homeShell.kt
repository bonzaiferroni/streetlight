package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyStar
import streetlight.web.pages.appFooter

fun FlowContent.homeShell(content: HomeContent) {
    column(HomeShell.homeBoxId) {
        geoMapMount()

        // val galaxyStars = content.galaxies.map { GalaxyStar(it.pathId, it.name, it.imageUrl) }

        val galaxy = content.galaxies.first()

        // button()
//        carousel {
//            content.galaxies.forEach {
//                buttonOf(it)
//            }
//
//            content.galaxies.forEach {
//                buttonOf(it)
//            }
//
//            content.galaxies.forEach {
//                buttonOf(it)
//            }
//
//            content.galaxies.forEach {
//                buttonOf(it)
//            }
//
//            content.galaxies.forEach {
//                buttonOf(it)
//            }
//        }

        column {
            heading3("Galaxies")
            textBlock("Galaxies are Streetlight communities, each with a particular focus.", modify(Dim))
            content.galaxies.forEach { galaxy ->
                cardOf(galaxy)
            }
        }

        gridOf(content.posts)

        appFooter()
    }
}

object HomeShell {
    val homeBoxId = Id("home-box")
    val tabsId = Id("home-tabs")
}