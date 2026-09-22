package streetlight.web.shells

import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyContent
import streetlight.web.layouts.renderLayout
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader
import streetlight.web.ui.BodyStyle

fun FlowContent.galaxyShell(content: GalaxyContent) {
    column(BodyStyle.ShellColumn) {
        appHeader()
        section(BodyStyle.MainColumn) {
            renderLayout(content)
            appFooter()
        }
    }

    dataIsland(GalaxyShell.islandId, content)
}

object GalaxyShell {
    val islandId = Id("galaxy-shell__island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/galaxyShell.kt"
}
