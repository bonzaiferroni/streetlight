package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.serialization.Serializable
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyContent
import streetlight.model.data.GalaxyPost
import streetlight.web.EarthMapRoute
import streetlight.web.layouts.postSection
import streetlight.web.pages.appFooter
import streetlight.web.ui.EarthKey
import streetlight.web.ui.headerOf

fun FlowContent.galaxyShell(content: GalaxyContent) {
    val galaxy = content.galaxy; val post = content.posts;
    column(GalaxyKey.ShellId) {
        headerOf(galaxy)
        box {
            btn("View Map", EarthMapRoute(galaxy.slug), EarthKey.ViewMapButtonMod)
            column(modify(Gap8)) {
                row {
                    galaxyMenu(emptyList(), galaxy)
                    filigree(modify(Flex1), MaxWidthNone) { spacer { setStyle(Property.Width.to("8rem")) } }
                    createPostMenu(galaxy)
                }
//                if (galaxy.postTypes.contains(PostType.Content)) {
//                    layoutTalkPreview(TalkRoute(galaxy.galaxyId), listing.comments)
//                }
                postSection(post)
                appFooter(GalaxyKey.SOURCE)
            }
        }
    }

    dataIsland(GalaxyKey.GalaxyContentId, content)
}

object GalaxyKey {
    val ShellId = Id("galaxy-profile-shell")
    val GalaxyContentId = Id("galaxy-island")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/galaxyShell.kt"
}