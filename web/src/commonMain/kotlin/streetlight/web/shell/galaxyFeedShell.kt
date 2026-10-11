package streetlight.web.shell

import koala.SiteImage
import koala.page.*
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyFeedContent
import streetlight.model.ui.GalaxyFeedRoute
import streetlight.web.layout.feedSection
import streetlight.web.layout.mainBody
import streetlight.web.layout.pageHeader

fun FlowContent.galaxyFeedShell(content: GalaxyFeedContent) {
    mainBody("galaxyFeedShell.kt") {
        pageHeader("Galaxies", "Streetlight Communities", SiteImage.HelixNebula)
        feedSection(content.feed) { GalaxyFeedRoute(it) }
    }

    dataIsland(GalaxyFeedShell.IslandId, content)
}

object GalaxyFeedShell {
    val IslandId = Id("galaxy-feed-shell__island")
}
