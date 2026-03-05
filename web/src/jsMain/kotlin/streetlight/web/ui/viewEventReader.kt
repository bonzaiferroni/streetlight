package streetlight.web.ui

import kampfire.utils.takeEllipsis
import koala.css.*
import koala.dom.*
import koala.html.propertyValue
import koala.model.mapDistinct
import streetlight.model.data.EventEdit
import streetlight.model.data.MultiEventParseResponse
import streetlight.web.EditEventCallbackRoute
import streetlight.web.ReadEventRoute
import streetlight.web.model.AppContext
import streetlight.web.model.EventReader

fun RenderContext.viewEventReader(app: AppContext, route: ReadEventRoute) {
    val model = EventReader(renderScope, app.client.api, route)
    val locationFlow = model.locationFlow
    val linkFlow = model.stateFlow.mapDistinct { it.link }
    val parseFlow = model.stateFlow.mapDistinct { it.parse }

    flowBlock(locationFlow, defaultMagic, magic = true) { location ->
        flowBlock(parseFlow, defaultMagic, magic = true) { parse ->
            if (parse != null && location != null) {
                val events = parse.events?.takeIf { it.isNotEmpty() }
                if (events != null) {
                    card {
                        row {
                            textBlock("If the information looks correct, you can post these events to ${location.name}.", modify(Flex1))
                            button("post all", onClick = model::postAll)
                        }

                        events.forEachIndexed { index, event ->
                            viewEventParse(event, index, model, app)
                        }
                    }
                }
            } else if (parse == null && location != null) {
                card {
                    row {
                        messageBox(model.messageFlow, modify(Flex1))
                        button("start over", onClick = model::startOver)
                        button("find events", onClick = model::readCalendar)
                    }
                }
            } else if (parse != null && location == null) {
                card {
                    row {
                        messageBox(model.messageFlow, modify(Flex1))
                        button("start over", onClick = model::startOver)
                    }
                }
            } else {
                card {
                    column {
                        row {
                            messageBox(model.messageFlow, modify(Flex1))
                            button("📃 Use form")
                        }
                        row {
                            textField("link", modify(Flex1), model::setLink, linkFlow)
                            button("🤖 read link", onClick = model::readLink)
                        }
                    }
                }
            }
        }
    }
}

fun RenderContext.viewEventReader(app: AppContext) {
    routeBlock<ReadEventRoute>(app.portal) { route ->
        viewEventReader(app, route)
    }
}

private fun RenderContext.viewEventParse(
    event: EventEdit,
    index: Int,
    model: EventReader,
    app: AppContext,
) {
    val eventName = event.title
    val date = event.date
    val statusFlow = model.stateFlow.mapDistinct { it.getStatus(index) }

    val itemElement = card {
        flowBlock(statusFlow) { status ->
            if (status == 200) {
                textBlock("Posted: $eventName ✅")
            } else if (status == 409) {
                textBlock("Already posted: $eventName 👍")
            } else {
                row {
                    event.imageUrl?.takeIf { it.startsWith("http") }?.let {
                        image(it, modify(Width16))
                    }
                    column(modify(Flex1)) {
                        // name
                        textBlock(eventName)
                        // time/date
                        row {
                            textBlock(date.toString())
                            event.startsAt?.let { time ->
                                textBlock(time.toString())
                            }
                        }
                        // description
                        event.description?.let {
                            propertyValue("description", it.takeEllipsis(200))
                        }
                        // ageMin
                        event.ageMin?.let {
                            propertyValue("ages", "$it+")
                        }
                        // contact
                        event.contact?.let {
                            propertyValue("contact", it)
                        }
                        // url
                        event.url?.takeIf { it.startsWith("http") }?.let {
                            propertyValue("url", it)
                        }
                    }
                }
            }
        }
    }

    itemElement.onClick {
        val route = EditEventCallbackRoute(event) { event ->
            if (event != null) {
                model.setCompleted(index)
            }
            itemElement.scrollWhenPresent()
        }
        app.portal.go(route)
    }
}