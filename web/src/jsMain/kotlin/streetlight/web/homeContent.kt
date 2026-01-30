package streetlight.web

import koala.css.AlignItemsCenter
import koala.css.Width100
import koala.html.column
import koala.html.geoMap
import koala.html.tab
import koala.dom.tabs
import koala.html.Id
import streetlight.web.pages.homeFooter

fun RenderContext.homeContent() {
    tabs(Id("main-tabs"), Width100) {
        tab("Events") {
            eventsTab()
        }
        tab("Map") {
            column(AlignItemsCenter) {
                geoMap()
                homeFooter()
            }
        }
        tab("App") {
            appDescription()
        }
    }
}