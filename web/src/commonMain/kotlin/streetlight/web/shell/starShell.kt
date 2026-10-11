package streetlight.web.shell

import koala.page.Id
import koala.page.dataIsland
import kotlinx.html.FlowContent
import streetlight.model.data.StarContent
import streetlight.web.layout.renderLayout
import streetlight.web.layout.mainBody

fun FlowContent.starShell(content: StarContent) {
    mainBody("starShell.kt") {
        renderLayout(content)
    }

    dataIsland(StarShell.islandId, content)
}

object StarShell {
    val islandId = Id("star-shell-island")
    const val SourcePath = "web/src/commonMain/kotlin/streetlight/web/shell/starShell.kt"
}
