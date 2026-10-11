package streetlight.web.shell

import koala.SiteImage
import koala.page.*
import kotlinx.html.FlowContent
import streetlight.model.data.CityFeedContent
import streetlight.model.ui.CityFeedRoute
import streetlight.web.layout.feedSection
import streetlight.web.layout.mainBody
import streetlight.web.layout.pageHeader

fun FlowContent.cityFeedShell(content: CityFeedContent) {
    mainBody("cityFeedShell.kt") {
        pageHeader("Cities", "Streetlight Cities", SiteImage.PearlStreet)
        feedSection(content.feed) { CityFeedRoute(it) }
    }

    dataIsland(CityFeedShell.IslandId, content)
}

object CityFeedShell {
    val IslandId = Id("city-feed-shell__island")
}
