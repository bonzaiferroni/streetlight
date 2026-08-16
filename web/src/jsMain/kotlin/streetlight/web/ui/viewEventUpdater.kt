package streetlight.web.ui

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.tabs
import streetlight.model.data.EventEdit
import streetlight.model.data.EventUpdaterContent
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.EventRoute
import streetlight.model.ui.EventUpdateRoute

fun ViewScope.viewEventUpdater(content: EventUpdaterContent, star: Star) {
    val edit = content.event.toEdit()
    val model = edit.let { app.getEventEditor(it, contentScope) }

    column(BodyStyle.Column) {
        tabs {
            tab("edit") {
                column {
                    updaterGreeting(star, model.stateNow.edit.title ?: "this event")
                    eventEditFormBody(model)
                    formSubmitLegacy(
                        label = "Save",
                        onSubmit = {
                            launchEffect {
                                val event = model.submitSuspend()
                                if (event != null) {
                                    portal.go(EventRoute(event.slug))
                                }
                            }
                        },
                        messages = model.message,
                        back = LabeledAction("go back", portal::goBack)
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
        appFooter("")
    }
}

fun RouteScope.viewEventUpdaterRoute() {
    routeBlock<EventUpdateRoute, EventUpdaterContent> { content ->
        starGate { star ->
            viewEventUpdater(content, star)
        }
    }
}