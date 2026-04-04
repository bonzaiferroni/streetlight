package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.serialization.Serializable
import streetlight.model.data.Galaxy
import streetlight.model.data.PostListing
import streetlight.web.layouts.layoutPostListing
import streetlight.web.pages.appFooter
import streetlight.web.ui.galaxyHeader

fun FlowContent.galaxyProfileShell(content: GalaxyProfileContent) {
    val galaxy = content.galaxy; val listing = content.listing;
    column(GalaxyProfileKey.ShellId, modify(Gap8)) {
        column {
            swapBlock(GalaxyProfileKey.SwapId, modify(Magic, OverflowClip)) {
                galaxyHeader(galaxy, modify(SlideLeft)) {
                    setId(GalaxyProfileKey.HeaderId)
                    setReveal(true)
                }
                geoMapMount(galaxy.center, modify(SlideRight)) {
                    setId(GalaxyProfileKey.MapId)
                    setReveal(false)
                }
            }
            row(modify(JustifyContentSpaceBetween)) {
                row {
                    galaxyMenu(emptyList(), galaxy)
                    switch("map", id = GalaxyProfileKey.MapSwitchId)
                }
                postMenu(galaxy)
            }
        }
        layoutPostListing(listing)
        appFooter(GalaxyProfileKey.SOURCE)
    }
}

object GalaxyProfileKey {
    val ShellId = Id("galaxy-profile-shell")
    val SwapId = Id("galaxy-profile-swap")
    val HeaderId = Id("galaxy-profile-header")
    val MapId = Id("galaxy-profile-map")
    val MapSwitchId = Id("galaxy-profile-map-switch")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/galaxyProfileShell.kt"
}

@Serializable
data class GalaxyProfileContent(
    val galaxy: Galaxy,
    val listing: PostListing,
)