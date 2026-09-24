package streetlight.web.shells

import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyContent
import streetlight.web.layouts.renderLayout
import streetlight.web.ui.mainBody

fun FlowContent.galaxyShell(content: GalaxyContent) {
    mainBody("galaxyShell.kt") {
        renderLayout(content)
    }

    dataIsland(GalaxyShell.islandId, content)
}

object GalaxyShell {
    val islandId = Id("galaxy-shell__island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/galaxyShell.kt"
}
