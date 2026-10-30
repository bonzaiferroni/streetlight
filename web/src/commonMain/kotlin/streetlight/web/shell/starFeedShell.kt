package streetlight.web.shell

import koala.SiteImage
import koala.page.*
import kotlinx.html.FlowContent
import streetlight.model.data.StarFeedContent
import streetlight.model.ui.StarFeedRoute
import streetlight.web.layout.feedSection
import streetlight.web.layout.mainBody
import streetlight.web.layout.pageHeader

fun FlowContent.starFeedShell(content: StarFeedContent) {
    mainBody("starFeedShell.kt") {
        pageHeader("Stars", "Streetlight Stars", SiteImage.HelixNebula)
        feedSection(content.feed) { StarFeedRoute(it) }
    }

    dataIsland(StarFeedShell.IslandId, content)
}

object StarFeedShell {
    val IslandId = Id("star-feed-shell__island")
}
