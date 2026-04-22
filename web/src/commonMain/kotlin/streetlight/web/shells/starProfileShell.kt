package streetlight.web.shells

import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import streetlight.model.data.PostListing
import streetlight.model.data.PostType
import streetlight.model.data.Star
import streetlight.web.layouts.layoutPostListing
import streetlight.web.pages.appFooter
import streetlight.web.ui.starHeader

fun FlowContent.starProfileShell(content: StarProfileContent) {
    val star = content.star; val listing = content.listing;
    column(StarProfileKey.ShellId, modify(Gap8)) {
        column {
            swapBlock(StarProfileKey.SwapId, modify(Magic, OverflowClip)) {
                starHeader(star, modify(SlideLeft)) {
                    setId(StarProfileKey.HeaderId)
                    setReveal(true)
                }
                geoMapMount(null, modify(SlideRight)) {
                    setId(StarProfileKey.MapId)
                    setReveal(false)
                }
            }
        }
        layoutPostListing(setOf(PostType.Event, PostType.Location), listing)
        appFooter(StarProfileKey.SOURCE)
    }
}

data class StarProfileContent(
    val star: Star,
    val listing: PostListing,
)

object StarProfileKey {
    val ShellId = Id("star-profile-shell")
    val SwapId = Id("star-profile-swap")
    val HeaderId = Id("star-profile-header")
    val MapId = Id("star-profile-map")
    val MapSwitchId = Id("star-profile-map-switch")
    const val SOURCE = "web/src/commonMain/kotlin/streetlight/web/shells/starProfileShell.kt"
}