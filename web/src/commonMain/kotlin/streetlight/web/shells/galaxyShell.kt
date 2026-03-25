package streetlight.web.shells

import koala.css.Accent
import koala.css.DisplayNone
import koala.css.JustifySpaceBetween
import koala.css.StyleProperty
import koala.css.modify
import koala.css.setStyle
import koala.html.Id
import koala.html.box
import koala.html.btn
import koala.html.column
import koala.html.geoMapMount
import koala.html.row
import koala.html.setId
import koala.html.swapBlock
import koala.html.tab
import koala.html.tabs
import kotlinx.css.Display
import kotlinx.html.FlowContent
import kotlinx.serialization.Serializable
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.web.EventScoutRoute
import streetlight.web.pages.appFooter
import streetlight.web.ui.headerOf

fun FlowContent.galaxyShell(content: GalaxyProfileContent) {
    val galaxy = content.galaxy; val posts = content.posts;
    column(GalaxyProfileKey.ShellId) {
        swapBlock(GalaxyProfileKey.SwapId) {
            headerOf(galaxy) {
                setId(GalaxyProfileKey.HeaderId)
            }
            geoMapMount {
                setId(GalaxyProfileKey.MapId)
                setStyle(StyleProperty.display.to(Display.none))
            }
        }
        row(modify(JustifySpaceBetween)) {
            galaxyMenu(content.galaxies)
            btn("Post Event", EventScoutRoute(galaxy.pathId), modify(Accent))
        }
        gridOf(content.posts)
        appFooter()
    }
}

object GalaxyProfileKey {
    val ShellId = Id("galaxy-profile-shell")
    val SwapId = Id("galaxy-profile-swap")
    val HeaderId = Id("galaxy-profile-header")
    val MapId = Id("galaxy-profile-map")
}

@Serializable
data class GalaxyProfileContent(
    val galaxy: Galaxy,
    val posts: List<GalaxyPost>,
    val galaxies: List<Galaxy>
)