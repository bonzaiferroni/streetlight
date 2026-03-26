package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.serialization.Serializable
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.web.EventScoutRoute
import streetlight.web.layouts.layoutGalaxyPosts
import streetlight.web.pages.appFooter
import streetlight.web.ui.headerOf

fun FlowContent.galaxyShell(content: GalaxyProfileContent) {
    val galaxy = content.galaxy; val posts = content.posts;
    column(GalaxyProfileKey.ShellId) {
        swapBlock(GalaxyProfileKey.SwapId, modify(Magic, OverflowClip)) {
            headerOf(galaxy, modify(SlideLeft)) {
                setId(GalaxyProfileKey.HeaderId)
                setReveal(true)
            }
            geoMapMount(galaxy.center, modify(SlideRight)) {
                setId(GalaxyProfileKey.MapId)
                setReveal(false)
            }
        }
        row(modify(JustifySpaceBetween)) {
            row {
                galaxyMenu(content.galaxies, galaxy)
                switch("map", id = GalaxyProfileKey.MapSwitchId)
            }
            btn("Post Event", EventScoutRoute(galaxy.pathId), modify(Accent))
        }
        layoutGalaxyPosts(posts)
        appFooter()
    }
}

object GalaxyProfileKey {
    val ShellId = Id("galaxy-profile-shell")
    val SwapId = Id("galaxy-profile-swap")
    val HeaderId = Id("galaxy-profile-header")
    val MapId = Id("galaxy-profile-map")
    val MapSwitchId = Id("galaxy-profile-map-switch")
}

@Serializable
data class GalaxyProfileContent(
    val galaxy: Galaxy,
    val posts: List<GalaxyPost>,
    val galaxies: List<Galaxy>
)