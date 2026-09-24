package streetlight.web.shells

import koala.html.Id
import koala.html.dataIsland
import kotlinx.html.FlowContent
import streetlight.model.data.Media
import streetlight.web.layouts.renderLayout
import streetlight.web.ui.mainBody

fun FlowContent.mediaShell(media: Media) {
    mainBody("mediaShell.kt") {
        renderLayout(media)
    }

    dataIsland(MediaShell.IslandId, media)
}

object MediaShell {
    val IslandId = Id("post-data")
    val TalkId = Id("post-talk")
}

