package streetlight.web.shell

import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyContent
import streetlight.web.layout.renderLayout
import streetlight.web.layout.mainBody
import koala.page.*

fun FlowContent.galaxyShell(content: GalaxyContent) {
    mainBody("galaxyShell.kt") {
        renderLayout(content)
    }

    dataIsland(GalaxyShell.islandId, content)
}

object GalaxyShell {
    val islandId = Id("galaxy-shell__island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shell/galaxyShell.kt"
}
