package streetlight.web.shells

import koala.SvgFile
import koala.html.AppRoute
import koala.html.IconRoute
import koala.html.Id
import koala.html.column
import koala.html.dataIsland
import koala.html.routeMenu
import kotlinx.html.FlowContent
import streetlight.model.data.Star
import streetlight.model.data.StarContent
import streetlight.model.ui.StarDashRoute
import streetlight.model.ui.UpdateProfileRoute
import streetlight.model.ui.StarRoute
import streetlight.model.ui.UpdateAccountRoute
import streetlight.web.layouts.postSection
import streetlight.web.pages.appFooter
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.featureHeader

fun FlowContent.starShell(content: StarContent) {
    val star = content.star
    column(StarShell.id, BodyStyle.MainColumn) {
        featureHeader(
            title = star.username.value,
            descriptor = "a streetlighter",
            image = star.image,
            subtitle = star.tagline,
            description = star.description,
        )

        postSection(content.posts)

        appFooter(StarShell.SourcePath)

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
    val rightIcons = when (isCaller) {
        true -> listOf(
            IconRoute(SvgFile.MessageSmall, StarDashRoute),
            IconRoute(SvgFile.UserSmall, UpdateAccountRoute)
        )
        else -> null
    }

    val routes = when (isCaller) {
        true -> listOf(StarRoute(star.username), UpdateProfileRoute)
        else -> listOf(StarRoute(star.username))
    }

    routeMenu(
        context = star.username.value,
        routeNow = routeNow,
        routes = routes,
        rightIcons = rightIcons
    )
}