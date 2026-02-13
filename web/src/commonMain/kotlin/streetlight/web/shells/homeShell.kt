package streetlight.web.shells

import koala.css.AlignItemsCenter
import koala.css.Width100
import koala.css.modify
import koala.html.GeoMapSelector
import koala.html.Id
import koala.html.action
import koala.html.box
import koala.html.button
import koala.html.column
import koala.html.geoMapMount
import koala.html.tab
import koala.html.tabs
import koala.html.textBlock
import kotlinx.html.FlowContent
import kotlinx.html.footer
import streetlight.model.data.Event
import streetlight.web.EventObjectRoute
import streetlight.web.SandboxRoute
import streetlight.web.pages.appFooter

fun FlowContent.homeShell(events: List<Event> = emptyList()) {
    box(HomeShell.homeBoxId) {
        tabs(HomeShell.tabsId, modify(Width100)) {
            tab("Events") {
                eventsTab(events)
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

fun FlowContent.eventsTab(events: List<Event>) {
    column(modify(AlignItemsCenter)) {
        events.forEach { event ->
            action(EventObjectRoute(event)) {
                textBlock(event.title)
            }
        }
        button("Go to sandbox", SandboxRoute)
        appFooter()
    }
}