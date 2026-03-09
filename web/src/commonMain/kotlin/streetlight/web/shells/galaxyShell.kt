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
import streetlight.model.data.Event
import streetlight.model.data.Galaxy
import streetlight.web.EventScoutRoute
import streetlight.web.ui.headerOf

fun FlowContent.galaxyShell(galaxy: Galaxy, events: List<Event>) {
    box(GalaxyShell.galaxyBoxId) {
        tabs(HomeShell.tabsId) {
            tab("Events") {
                column {
                    headerOf(galaxy)
                    row {
                        box(modify(Flex1))
                        button("Post Event", EventScoutRoute(galaxy.pathId))
                    }
                }
            }
            tab("Map") {
                column {
                    geoMapMount()
                }
            }
            tab("Talk") {

            }
        }
    }
}

object GalaxyShell {
    val galaxyBoxId = Id("galaxy-box")
}