package streetlight.web.shells

import koala.html.Id
import koala.html.column
import koala.html.dataIsland
import koala.html.section
import kotlinx.html.FlowContent
import streetlight.model.data.StarContent
import streetlight.web.layouts.renderLayout
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader
import streetlight.web.ui.BodyStyle

fun FlowContent.starShell(content: StarContent) {
    column(BodyStyle.ShellColumn) {
        appHeader()

        section(BodyStyle.MainColumn) {
            renderLayout(content)
            appFooter()
        }
    }

    dataIsland(StarShell.islandId, content)
}

object StarShell {
    val islandId = Id("star-shell-island")
    const val SourcePath = "web/src/commonMain/kotlin/streetlight/web/shells/starShell.kt"
}
