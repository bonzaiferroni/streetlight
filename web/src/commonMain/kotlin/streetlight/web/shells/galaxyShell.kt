package streetlight.web.shells

import koala.SvgFile
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyContent
import streetlight.model.ui.HomeRoute
import streetlight.web.pages.appFooter
import streetlight.model.ui.toConfigRoute
import streetlight.model.ui.toEarthRoute
import streetlight.model.ui.toRoute
import streetlight.web.layouts.renderLayout
import streetlight.web.pages.appHeader
import streetlight.web.ui.BodyStyle

fun FlowContent.galaxyShell(content: GalaxyContent) {
    val galaxy = content.galaxy
    column(GalaxyShell.id, BodyStyle.ShellColumn) {
        appHeader()
        section(BodyStyle.MainColumn) {
            renderLayout(content)
            appFooter()
        }
        val routeNow = galaxy.toRoute()
        routeMenu(
            galaxy.name, routeNow, listOf(routeNow, galaxy.toEarthRoute()),
            leftIcons = listOf(IconRoute(SvgFile.Home, HomeRoute)),
            rightIcons = listOf(IconRoute(SvgFile.GearSmall, galaxy.toConfigRoute()))
        )
    }

    dataIsland(GalaxyShell.islandId, content)
}

object GalaxyShell {
    val id = Id("galaxy-shell")
    val islandId = Id("galaxy-shell__island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/galaxyShell.kt"
}