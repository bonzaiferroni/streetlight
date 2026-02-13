package streetlight.web.shells

import koala.css.AlignItemsCenter
import koala.css.Width100
import koala.css.modify
import koala.html.Id
import koala.html.button
import koala.html.column
import koala.html.tab
import koala.html.tabs
import kotlinx.html.FlowContent
import kotlinx.html.footer
import streetlight.web.SandboxRoute
import streetlight.web.pages.appFooter

fun FlowContent.homeShell() {
    tabs(HomeShell.tabsId, modify(Width100)) {
        tab("Events") {
            eventsTab()
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

object HomeShell {
    val tabsId = Id("home-tabs")
}

fun FlowContent.eventsTab() {
    column(modify(AlignItemsCenter)) {
//        events.forEach { event ->
//            a {
//                href = "/event-portal/${event.eventId.value}"
//                card {
//                    label(event.title)
//                }
//            }
//        }
        button("Go to sandbox", SandboxRoute)
        appFooter()
    }
}