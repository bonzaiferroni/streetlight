package streetlight.web

import koala.css.AlignItemsCenter
import koala.css.FillWidth
import koala.html.column
import koala.html.geoMap
import koala.html.tab
import koala.dom.tabs
import streetlight.web.pages.homeFooter

fun RenderContext.homeContent() {
    tabs(FillWidth) {
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
            appTab()
        }
    }
}