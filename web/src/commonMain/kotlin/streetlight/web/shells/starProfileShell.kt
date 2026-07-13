package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.GalaxyPost
import streetlight.model.data.Star
import streetlight.web.pages.appFooter
import streetlight.web.ui.headerOf

@Deprecated("use starShell")
fun FlowContent.starProfileShell(content: StarProfileContent) {
    val star = content.star; val listing = content.posts;
    column(StarProfileKey.ShellId, modify(Gap8)) {
        column {
            swapBlock(StarProfileKey.SwapId, modify(Magic, OverflowClip)) {
                headerOf(star, modify(SlideLeft)) {
                    setId(StarProfileKey.HeaderId)
                    setReveal(true)
                }
                geoMapMount(null, modify(SlideRight)) {
                    setId(StarProfileKey.MapId)
                    setReveal(false)
                }
            }
        }
        // layoutPostListing(setOf(PostType.Event, PostType.Location), listing)
        appFooter(StarProfileKey.SOURCE)
    }
}

@Deprecated("use StarContent")
data class StarProfileContent(
    val star: Star,
    val posts: List<GalaxyPost>,
)

object StarProfileKey {
    val ShellId = Id("star-profile-shell")
    val SwapId = Id("star-profile-swap")
    val HeaderId = Id("star-profile-header")
    val MapId = Id("star-profile-map")
    val MapSwitchId = Id("star-profile-map-switch")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/starProfileShell.kt"
}