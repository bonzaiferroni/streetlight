package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.serialization.Serializable
import streetlight.model.data.Galaxy
import streetlight.model.data.PostListing
import streetlight.web.EarthMapRoute
import streetlight.web.layouts.layoutPostListing
import streetlight.web.pages.appFooter
import streetlight.web.ui.EarthKey
import streetlight.web.ui.galaxyHeader

fun FlowContent.galaxyProfileShell(content: GalaxyProfileContent) {
    val galaxy = content.galaxy; val listing = content.listing;
    column(GalaxyProfileKey.ShellId) {
        galaxyHeader(galaxy, modify(BorderRadius2, Height48, MoonShadow))
        box {
            btn("View Map", EarthMapRoute(galaxy.slug), EarthKey.ViewMapButtonMod)
            column(modify(Gap8)) {
                row(modify(JustifyContentSpaceBetween)) {
                    galaxyMenu(emptyList(), galaxy)
                    postMenu(galaxy)
                }
                layoutPostListing(listing)
                appFooter(GalaxyProfileKey.SOURCE)
            }
        }
    }
}

object GalaxyProfileKey {
    val ShellId = Id("galaxy-profile-shell")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/galaxyProfileShell.kt"
}

@Serializable
data class GalaxyProfileContent(
    val galaxy: Galaxy,
    val listing: PostListing,
)