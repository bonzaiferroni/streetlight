package streetlight.web

import kampfire.model.GeoPoint
import koala.css.AlignItemsCenter
import koala.css.Width100
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.tabs
import koala.html.Id
import koala.html.column
import koala.html.tab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun RenderContext.viewHome(app: AppContext) {
    console.log("loading home")

    tabs(Id("main-tabs"), modify(Width100)) {
        tab("Events") {
            eventsTab(app.portal)
        }
        tab("Map") {
            column(modify(AlignItemsCenter)) {
                viewStreetMap(app)
                footer()
            }
        }
        tab("App") {
            appDescription()
        }
    }
}