package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.AppScope
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.tab
import koala.dom.tabs
import koala.dom.textBlock
import streetlight.model.data.EventEdit
import streetlight.model.data.EventUpdaterContent
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.EventRoute
import streetlight.model.ui.EventUpdateRoute

fun AppScope.viewEventUpdater(content: EventUpdaterContent, star: Star) {
    val edit = content.event.toEdit()
    val model = edit.let { app.getEventEditor(it, parentScope) }

    tabs {
        tab("edit") {
            column {
                updaterGreeting(star, model.stateNow.edit.title ?: "this event")
                eventEditFormBody(model)
                formSubmit(
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
}

fun AppScope.viewEventUpdaterRoute() {
    column {
        starGate { star ->
            routeBlock<EventUpdateRoute, EventUpdaterContent?>(
                portal = portal,
                provideData = { route ->
                    api.readEventUpdaterContent(route.slug).handleResponse(toaster::toast)
                }
            ) { content ->
                if (content == null) {
                    textBlock("something went wrong")
                    return@routeBlock
                }

                viewEventUpdater(content, star)
            }
        }
        appFooter("")
    }
}