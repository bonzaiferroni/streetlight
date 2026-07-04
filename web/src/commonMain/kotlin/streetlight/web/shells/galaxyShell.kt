package streetlight.web.shells

import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyContent
import streetlight.web.HomeRoute
import streetlight.web.layouts.postSection
import streetlight.web.pages.appFooter
import streetlight.web.toConfigRoute
import streetlight.web.toEarthRoute
import streetlight.web.toRoute
import streetlight.web.ui.headerOf

fun FlowContent.galaxyShell(content: GalaxyContent) {
    val galaxy = content.galaxy; val post = content.posts;
    column(GalaxyKey.ShellId, modify(MarginTop1)) {
        headerOf(galaxy)
        box {
            // btn("View Map", EarthRoute(galaxy.slug), EarthStyle.ViewMapButtonMod)
            column(modify(Gap8)) {
                row(modify(JustifyContentSpaceBetween)) {
                    galaxyMenu(emptyList(), galaxy)
                    // filigree(modify(Flex1), MaxWidthNone) { spacer { setStyle(Property.Width.to(8.rem)) } }
                    createPostMenu(galaxy)
                }
//                if (galaxy.postTypes.contains(PostType.Content)) {
//                    layoutTalkPreview(TalkRoute(galaxy.galaxyId), listing.comments)
//                }
                postSection(post)
                appFooter(GalaxyKey.SOURCE)
            }
        }

        val routeNow = galaxy.toRoute()
        routeMenu(
            galaxy.name, routeNow, listOf(routeNow, galaxy.toEarthRoute()),
            leftIcons = listOf(RouteMenuIcon(SvgFile.Home, HomeRoute)),
            rightIcons = listOf(RouteMenuIcon(SvgFile.GearSmall, galaxy.toConfigRoute()))
        )
    }

    dataIsland(GalaxyKey.GalaxyContentId, content)
}

object GalaxyKey {
    val ShellId = Id("galaxy-profile-shell")
    val GalaxyContentId = Id("galaxy-island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/galaxyShell.kt"
}