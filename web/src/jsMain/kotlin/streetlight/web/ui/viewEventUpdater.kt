package streetlight.web.ui

import koala.modifier.Accent
import koala.dom.MenuAction
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.lazyTabs
import koala.dom.routeBlock
import streetlight.model.data.EventEdit
import streetlight.model.data.EventUpdaterContent
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.EventRoute
import streetlight.model.ui.EventUpdateRoute

fun ViewScope.viewEventUpdater(content: EventUpdaterContent, star: Star) {
    val edit = content.event.toEdit()
    val model = edit.let { app.getEventEditor(it, contentScope) }

    configBody("Event", "Config", "viewEventUpdater.kt") {
        configHeading(content.event.title, EventRoute(content.event.slug))

        lazyTabs {
            tab("edit") {
                column {
                    eventEditFormBody(model)
                    formSubmit(
                        label = "Save",
                        onClick = {
                            launchEffect {
                                val event = model.submitSuspend()
                                if (event != null) {
                                    portal.go(EventRoute(event.slug))
                                }
                            }
                        },
                        messenger = model.message,
                        buttonMod = Accent,
                        back = MenuAction("go back", onClick = portal::goBack)
                    )
                }
            }
            tab("history") {
                viewEditHistory<EventEdit>(content.editLogs) { edit, compareEdit ->
                    deltaRow("title", edit.title, compareEdit?.title)
                    deltaRow("description", edit.description, compareEdit?.description)
                }
            }
        }
    }
}

fun RouteScope.viewEventUpdaterRoute() {
    routeBlock<EventUpdateRoute, EventUpdaterContent> { content ->
        starGate { star ->
            viewEventUpdater(content, star)
        }
    }
}