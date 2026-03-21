package streetlight.web.shells

import koala.css.AlignItemsCenter
import koala.css.Dim
import koala.css.Width100
import koala.css.modify
import koala.html.GeoMapSelector
import koala.html.Id
import koala.html.box
import koala.html.column
import koala.html.geoMapMount
import koala.html.heading3
import koala.html.tab
import koala.html.tabs
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.web.pages.appFooter

fun FlowContent.homeShell(content: HomeContent) {
    column(HomeShell.homeBoxId) {
        geoMapMount()

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