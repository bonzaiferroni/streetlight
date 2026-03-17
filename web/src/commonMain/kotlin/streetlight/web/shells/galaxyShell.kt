package streetlight.web.shells

import koala.css.Flex1
import koala.css.modify
import koala.html.Id
import koala.html.box
import koala.html.button
import koala.html.column
import koala.html.geoMapMount
import koala.html.row
import koala.html.tab
import koala.html.tabs
import kotlinx.html.FlowContent
import kotlinx.serialization.Serializable
import streetlight.model.data.Event
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.web.EventScoutRoute
import streetlight.web.pages.appFooter
import streetlight.web.ui.headerOf

fun FlowContent.galaxyShell(content: GalaxyShellContent) {
    val galaxy = content.galaxy; val posts = content.posts;
    box(GalaxyShell.galaxyBoxId) {
        tabs(HomeShell.tabsId) {
            tab("Posts") {
                column {
                    headerOf(galaxy)
                    row {
                        box(modify(Flex1))
                        button("Post Event", EventScoutRoute(galaxy.pathId))
                    }
                    posts.forEach { post ->
                        largeGridOf(post)
                    }
                    appFooter()
                }
            }
            tab("Map") {
                column {
                    geoMapMount()
                    box(GalaxyShell.mapPanelId)
                    appFooter()
                }
            }
//            tab("Talk") {
//
//            }
        }
    }
}

object GalaxyShell {
    val galaxyBoxId = Id("galaxy-box")
    val mapPanelId = Id("galaxy-map-panel")
}

@Serializable
data class GalaxyShellContent(
    val galaxy: Galaxy,
    val posts: List<GalaxyPost>
)