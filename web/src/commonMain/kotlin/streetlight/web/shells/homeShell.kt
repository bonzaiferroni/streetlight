package streetlight.web.shells

import koala.css.AlignItemsCenter
import koala.css.Width100
import koala.css.modify
import koala.html.GeoMapSelector
import koala.html.Id
import koala.html.box
import koala.html.column
import koala.html.geoMapMount
import koala.html.tab
import koala.html.tabs
import kotlinx.html.FlowContent
import kotlinx.html.footer
import streetlight.model.data.Event
import streetlight.model.data.Location

fun FlowContent.homeShell(content: SpotlightContent) {
    box(HomeShell.homeBoxId) {
        tabs(HomeShell.tabsId, modify(Width100)) {
            tab("Spotlight") {
                spotlightTab(content)
            }
            tab("Map") {
                column(modify(AlignItemsCenter)) {
                    geoMapMount()
                    box(GeoMapSelector.panel)
                    footer()
                }
            }
            tab("App") {
                aboutApp()
            }
        }
    }
}

object HomeShell {
    val homeBoxId = Id("home-box")
    val tabsId = Id("home-tabs")
}