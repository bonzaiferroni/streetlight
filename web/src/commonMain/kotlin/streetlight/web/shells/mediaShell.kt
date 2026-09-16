package streetlight.web.shells

import koala.html.Id
import koala.html.column
import koala.html.dataIsland
import koala.html.section
import kotlinx.html.FlowContent
import streetlight.model.data.Media
import streetlight.web.layouts.renderLayout
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader
import streetlight.web.ui.BodyStyle

fun FlowContent.mediaShell(media: Media) {
    column(BodyStyle.ShellColumn) {
        appHeader()

        section(BodyStyle.MainColumn) {
            renderLayout(media)
            appFooter()
        }
    }

    dataIsland(MediaShell.IslandId, media)
}

object MediaShell {
    val IslandId = Id("post-data")
    val TalkId = Id("post-talk")
}

