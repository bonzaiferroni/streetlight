package streetlight.web.shells

import koala.html.Id
import koala.html.dataIsland
import kotlinx.html.FlowContent
import streetlight.model.data.StarContent
import streetlight.web.layouts.renderLayout
import streetlight.web.ui.mainBody

fun FlowContent.starShell(content: StarContent) {
    mainBody("starShell.kt") {
        renderLayout(content)
    }

    dataIsland(StarShell.islandId, content)
}

object StarShell {
    val islandId = Id("star-shell-island")
    const val SourcePath = "web/src/commonMain/kotlin/streetlight/web/shells/starShell.kt"
}
