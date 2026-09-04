package streetlight.web.shells

import koala.SvgFile
import koala.css.modify
import koala.html.AppRoute
import koala.html.IconRoute
import koala.html.Id
import koala.html.column
import koala.html.dataIsland
import koala.html.routeMenu
import koala.html.section
import kotlinx.html.FlowContent
import streetlight.model.data.Star
import streetlight.model.data.StarContent
import streetlight.model.ui.StarDashRoute
import streetlight.model.ui.ProfileConfigRoute
import streetlight.model.ui.StarRoute
import streetlight.model.ui.UpdateAccountRoute
import streetlight.web.layouts.postSection
import streetlight.web.layouts.renderLayout
import streetlight.web.pages.appFooter
import streetlight.web.pages.appHeader
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.featureHeader

fun FlowContent.starShell(content: StarContent) {
    val star = content.star
    column(StarShell.id, BodyStyle.ShellColumn) {
        appHeader()

        section(BodyStyle.MainColumn) {
            renderLayout(content)
            appFooter()
        }

        val routeNow = StarRoute(star.username)
        starRouteMenu(star, routeNow, content.isCaller)
    }

    dataIsland(StarShell.islandId, content)
}

object StarShell {
    val id = Id("star-shell")
    val islandId = Id("star-shell-island")
    const val SourcePath = "web/src/commonMain/kotlin/streetlight/web/shells/starShell.kt"
}

fun FlowContent.starRouteMenu(star: Star, routeNow: AppRoute, isCaller: Boolean) {
    val routes = when (isCaller) {
        else -> listOf(StarRoute(star.username))
    }

    val rightIcons = when (isCaller) {
        true -> listOf(
            IconRoute(SvgFile.GearSmall, ProfileConfigRoute),
        )
        else -> null
    }

    routeMenu(
        context = star.username.value,
        routeNow = routeNow,
        routes = routes,
        rightIcons = rightIcons
    )
}