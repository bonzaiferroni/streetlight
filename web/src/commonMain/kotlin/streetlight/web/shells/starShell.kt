package streetlight.web.shells

import koala.SvgFile
import koala.html.IconRoute
import koala.html.Id
import koala.html.column
import koala.html.dataIsland
import koala.html.routeMenu
import kotlinx.html.FlowContent
import streetlight.model.data.StarContent
import streetlight.model.ui.StarConfigRoute
import streetlight.model.ui.StarRoute
import streetlight.web.layouts.postSection
import streetlight.web.pages.appFooter
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.featureHeader

fun FlowContent.starShell(content: StarContent) {
    val star = content.star
    column(StarShell.id, BodyStyle.Mod) {
        featureHeader(
            title = star.username.value,
            descriptor = "a streetlighter",
            image = star.image,
            subtitle = star.tagline,
            description = star.description,
        )

        postSection(content.posts)

        val routeNow = StarRoute(star.username)
        val rightIcons = when (content.isCaller) {
            true -> listOf(IconRoute(SvgFile.GearSmall, StarConfigRoute))
            else -> null
        }
        routeMenu(star.username.value, routeNow, listOf(routeNow), rightIcons = rightIcons)

        appFooter(StarShell.SourcePath)
    }

    dataIsland(StarShell.islandId, content)
}

object StarShell {
    val id = Id("star-shell")
    val islandId = Id("star-shell-island")
    const val SourcePath = "web/src/commonMain/kotlin/streetlight/web/shells/starShell.kt"
}